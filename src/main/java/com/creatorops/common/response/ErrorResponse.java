package com.creatorops.common.response;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Reusable error response model for REST APIs matching RFC 7807/custom standards.
 */
public record ErrorResponse(
    OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ValidationError> validationErrors
) {
    public record ValidationError(String field, String message) {}
}
