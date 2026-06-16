package com.creatorops.ai.exception;

/**
 * Exception thrown when the AI generation request fails due to upstream API errors,
 * network timeouts, or invalid configurations.
 */
public class AiGenerationException extends RuntimeException {
    
    public AiGenerationException(String message) {
        super(message);
    }

    public AiGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
