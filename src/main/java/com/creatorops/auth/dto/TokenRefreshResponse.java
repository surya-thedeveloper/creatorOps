package com.creatorops.auth.dto;

public record TokenRefreshResponse(
    String accessToken,
    String refreshToken
) {}
