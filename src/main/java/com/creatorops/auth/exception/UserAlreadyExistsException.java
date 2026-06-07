package com.creatorops.auth.exception;

/**
 * Thrown when a user attempts to register with an email address that is already active in database.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
