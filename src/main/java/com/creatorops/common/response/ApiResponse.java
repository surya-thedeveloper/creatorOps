package com.creatorops.common.response;

import java.time.OffsetDateTime;

/**
 * Generic response wrapper for successful API responses.
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    OffsetDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }
}
