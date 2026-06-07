package com.creatorops.auth.exception;

/**
 * Thrown when credentials validation fail during login authentication.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
