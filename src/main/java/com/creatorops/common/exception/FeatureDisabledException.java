package com.creatorops.common.exception;

public class FeatureDisabledException extends RuntimeException {
    public FeatureDisabledException(String message) {
        super(message);
    }
}
