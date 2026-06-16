package com.creatorops.ai.provider;

import com.creatorops.ai.config.AiProperties;
import com.creatorops.ai.exception.AiGenerationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * <h3>GeminiAIProvider</h3>
 * Concrete implementation of the AIProvider abstraction targeting Google Gemini API.
 * <p>
 * <h3>Why provider isolation is enforced</h3>
 * Encapsulating external HTTP communications inside this adapter ensures that business services
 * do not need to know endpoint details, credentials, or custom JSON structures of Google Gemini.
 */
public class GeminiAIProvider implements AIProvider {

    private final AiProperties properties;
    private final RestTemplate restTemplate;

    public GeminiAIProvider(AiProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    // Constructor to inject custom RestTemplate for testing
    public GeminiAIProvider(AiProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    private String callGemini(String prompt) {
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
