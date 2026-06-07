package com.creatorops.auth.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    UserResponse user
) {}
