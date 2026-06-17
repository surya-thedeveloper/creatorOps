package com.creatorops.reliability;

import com.creatorops.ai.config.AiProperties;
import com.creatorops.ai.exception.AiGenerationException;
import com.creatorops.ai.provider.GeminiAIProvider;
import com.creatorops.common.metrics.MetricsService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AiResilienceTests {

    private RestTemplate restTemplate;
    private AiProperties properties;
    private CircuitBreakerRegistry circuitBreakerRegistry;
    private RetryRegistry retryRegistry;
    private MetricsService metricsService;
    private GeminiAIProvider provider;

    @BeforeEach
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        metricsService = mock(MetricsService.class);

        properties = mock(AiProperties.class);
        AiProperties.GeminiProperties gemini = mock(AiProperties.GeminiProperties.class);
        when(properties.gemini()).thenReturn(gemini);
        when(gemini.apiKey()).thenReturn("test-api-key");
        when(gemini.model()).thenReturn("gemini-1.5-flash");

        circuitBreakerRegistry = CircuitBreakerRegistry.of(
            io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .minimumNumberOfCalls(3)
                .slidingWindowSize(5)
                .build()
        );

        retryRegistry = RetryRegistry.of(
            io.github.resilience4j.retry.RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofDefaults()) // 0ms delay for tests
                .build()
        );

        provider = new GeminiAIProvider(properties, restTemplate, circuitBreakerRegistry, retryRegistry, metricsService);
    }

    @Test
    public void testRetryBehaviorOnFailure() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini Outage"));

        assertThrows(AiGenerationException.class, () -> provider.generateBrainstorm("test prompt"));

        // Verify it was called 3 times total (1 original attempt + 2 retry attempts)
        verify(restTemplate, times(3)).postForEntity(anyString(), any(HttpEntity.class), any());
        verify(metricsService, times(2)).incrementRetryCount();
    }

    @Test
    public void testCircuitBreakerOpensAndFailsFast() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini Outage"));

        // Force failures to open the circuit breaker
        for (int i = 0; i < 3; i++) {
            assertThrows(AiGenerationException.class, () -> provider.generateBrainstorm("test prompt"));
        }

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("geminiAI");
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());

        // Call again and verify it fails fast due to CallNotPermittedException
        AiGenerationException ex = assertThrows(AiGenerationException.class, () -> provider.generateBrainstorm("test prompt"));
        assertTrue(ex.getMessage().contains("overloaded") || ex.getMessage().contains("unavailable"));

        // Clear interactions and assert RestTemplate is NOT invoked at all when circuit is OPEN
        reset(restTemplate);
        assertThrows(AiGenerationException.class, () -> provider.generateBrainstorm("test prompt"));
        verifyNoInteractions(restTemplate);
    }
}
