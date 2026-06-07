package com.creatorops.common.exception;

/**
 * Exception thrown when a requested domain resource is not found in database.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
