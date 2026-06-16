package com.creatorops.ai;

import com.creatorops.ai.config.AiProperties;
import com.creatorops.ai.exception.AiGenerationException;
import com.creatorops.ai.provider.GeminiAIProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiAIProviderTests {

    @Mock
    private RestTemplate restTemplate;

    private AiProperties properties;
    private GeminiAIProvider provider;

    @BeforeEach
    void setUp() {
        properties = new AiProperties("gemini", new AiProperties.GeminiProperties("test_api_key", "gemini-1.5-flash"));
        provider = new GeminiAIProvider(properties, restTemplate);
    }

    @Test
    void generateBrainstorm_Success() {
        GeminiAIProvider.GeminiResponse mockResponse = new GeminiAIProvider.GeminiResponse(
                List.of(new GeminiAIProvider.GeminiResponse.Candidate(
                        new GeminiAIProvider.GeminiResponse.Content(
                                List.of(new GeminiAIProvider.GeminiResponse.Part("Brainstorm Ideas Output"))
                        )
                ))
        );

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(GeminiAIProvider.GeminiResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        String result = provider.generateBrainstorm("Prompt context");

        assertNotNull(result);
        assertEquals("Brainstorm Ideas Output", result);
    }

    @Test
    void generateScript_Success() {
        GeminiAIProvider.GeminiResponse mockResponse = new GeminiAIProvider.GeminiResponse(
                List.of(new GeminiAIProvider.GeminiResponse.Candidate(
                        new GeminiAIProvider.GeminiResponse.Content(
                                List.of(new GeminiAIProvider.GeminiResponse.Part("Main Section Script Content"))
                        )
                ))
        );

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(GeminiAIProvider.GeminiResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        String result = provider.generateScript("Prompt script instruction context");

        assertNotNull(result);
        assertEquals("Main Section Script Content", result);
    }

    @Test
    void generate_MissingApiKey_ThrowsException() {
        AiProperties missingKeyProps = new AiProperties("gemini", new AiProperties.GeminiProperties(null, "gemini-1.5-flash"));
        GeminiAIProvider invalidProvider = new GeminiAIProvider(missingKeyProps, restTemplate);

        assertThrows(AiGenerationException.class, () -> invalidProvider.generateBrainstorm("Prompt"));
    }

    @Test
    void generate_RateLimit_ThrowsException() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(GeminiAIProvider.GeminiResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate Limit Exceeded"));

        AiGenerationException ex = assertThrows(AiGenerationException.class, () -> provider.generateBrainstorm("Prompt"));
        assertTrue(ex.getMessage().contains("rate limit exceeded"));
    }

    @Test
    void generate_Timeout_ThrowsException() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(GeminiAIProvider.GeminiResponse.class)))
                .thenThrow(new ResourceAccessException("Connection Timeout"));

        AiGenerationException ex = assertThrows(AiGenerationException.class, () -> provider.generateBrainstorm("Prompt"));
        assertTrue(ex.getMessage().contains("timed out or is unavailable"));
    }
}
