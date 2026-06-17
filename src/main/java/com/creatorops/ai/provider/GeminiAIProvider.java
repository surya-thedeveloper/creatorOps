package com.creatorops.ai.provider;

import com.creatorops.ai.config.AiProperties;
import com.creatorops.ai.exception.AiGenerationException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import com.creatorops.common.metrics.MetricsService;

import java.util.List;
import java.util.function.Supplier;

/**
 * <h3>GeminiAIProvider</h3>
 * Concrete implementation of the AIProvider abstraction targeting Google Gemini API.
 * <p>
 * <h3>Why provider isolation is enforced</h3>
 * Encapsulating external HTTP communications inside this adapter ensures that business services
 * do not need to know endpoint details, credentials, or custom JSON structures of Google Gemini.
 */
public class GeminiAIProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIProvider.class);

    private final AiProperties properties;
    private final RestTemplate restTemplate;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final MetricsService metricsService;

    public GeminiAIProvider(AiProperties properties) {
        this(properties, CircuitBreakerRegistry.ofDefaults(), RetryRegistry.ofDefaults(), null);
    }

    public GeminiAIProvider(AiProperties properties, RestTemplate restTemplate) {
        this(properties, restTemplate, CircuitBreakerRegistry.ofDefaults(), RetryRegistry.ofDefaults(), null);
    }

    public GeminiAIProvider(AiProperties properties, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, MetricsService metricsService) {
        this.properties = properties;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.metricsService = metricsService;
        this.restTemplate = new RestTemplate();
        configureTimeouts();
        registerResilienceListeners();
    }

    public GeminiAIProvider(AiProperties properties, RestTemplate restTemplate, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, MetricsService metricsService) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.metricsService = metricsService;
        configureTimeouts();
        registerResilienceListeners();
    }

    private void registerResilienceListeners() {
        if (metricsService == null) return;
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("geminiAI");
            Retry retry = retryRegistry.retry("geminiAI");

            circuitBreaker.getEventPublisher().onStateTransition(event -> {
                if (event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
                    log.warn("AI Circuit Breaker transition to OPEN: {}", event.getStateTransition());
                    metricsService.incrementCircuitOpen();
                }
            });

            retry.getEventPublisher().onRetry(event -> {
                log.info("AI Retry attempt #{} generated for event: {}", event.getNumberOfRetryAttempts(), event.getName());
                metricsService.incrementRetryCount();
            });
        } catch (Exception e) {
            log.error("Failed to register Resilience4j event listeners: {}", e.getMessage());
        }
    }

    private void configureTimeouts() {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000); // 5 seconds
            factory.setReadTimeout(15000);   // 15 seconds
            this.restTemplate.setRequestFactory(factory);
        } catch (Exception e) {
            log.warn("Could not configure timeouts on RestTemplate request factory: {}", e.getMessage());
        }
    }

    private String callGemini(String prompt) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("geminiAI");
        Retry retry = retryRegistry.retry("geminiAI");

        Supplier<String> supplier = () -> executeCall(prompt);
        Supplier<String> decorated = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        decorated = Retry.decorateSupplier(retry, decorated);

        try {
            return decorated.get();
        } catch (CallNotPermittedException cnpe) {
            log.error("AI Generation failed because the circuit breaker is OPEN.");
            throw new AiGenerationException("AI Generation service is temporarily overloaded. Please try again later.", cnpe);
        } catch (AiGenerationException age) {
            throw age;
        } catch (Exception ex) {
            log.error("AI Generation failed: {}", ex.getMessage());
            throw new AiGenerationException("AI Generation service is currently unavailable. Please try again later.", ex);
        }
    }

    private String executeCall(String prompt) {
        String apiKey = properties.gemini().apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiGenerationException("API key for Google Gemini is not configured. Please set the CREATOROPS_AI_GEMINI_API_KEY environment variable.");
        }

        String model = properties.gemini().model();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        // Build Gemini request JSON payload
        GeminiRequest requestPayload = new GeminiRequest(
                List.of(new GeminiRequest.Content(
                        List.of(new GeminiRequest.Part(prompt))
                ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(url, entity, GeminiResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GeminiResponse body = response.getBody();
                if (body.candidates() != null && !body.candidates().isEmpty()) {
                    GeminiResponse.Candidate candidate = body.candidates().get(0);
                    if (candidate.content() != null && candidate.content().parts() != null && !candidate.content().parts().isEmpty()) {
                        return candidate.content().parts().get(0).text();
                    }
                }
                throw new AiGenerationException("Gemini API response structure is empty or unexpected.");
            } else {
                throw new AiGenerationException("Upstream Gemini API returned error: status " + response.getStatusCode());
            }
        } catch (org.springframework.web.client.HttpClientErrorException hex) {
            if (hex.getStatusCode() == org.springframework.http.HttpStatus.FORBIDDEN) {
                throw new AiGenerationException("AI provider authentication failed: Invalid API Key.", hex);
            } else if (hex.getStatusCode() == org.springframework.http.HttpStatus.TOO_MANY_REQUESTS) {
                throw new AiGenerationException("AI provider rate limit exceeded. Please try again later.", hex);
            } else {
                throw new AiGenerationException("Upstream AI provider error: " + hex.getMessage(), hex);
            }
        } catch (org.springframework.web.client.ResourceAccessException tex) {
            throw new AiGenerationException("AI provider connection timed out or is unavailable.", tex);
        } catch (Exception ex) {
            throw new AiGenerationException("Upstream AI provider error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String generateBrainstorm(String prompt) {
        return callGemini(prompt);
    }

    @Override
    public String generateScript(String prompt) {
        return callGemini(prompt);
    }

    // Nested private/package-private record structures for mapping request/response
    public record GeminiRequest(List<Content> contents) {
        public record Content(List<Part> parts) {}
        public record Part(String text) {}
    }

    public record GeminiResponse(List<Candidate> candidates) {
        public record Candidate(Content content) {}
        public record Content(List<Part> parts) {}
        public record Part(String text) {}
    }
}
