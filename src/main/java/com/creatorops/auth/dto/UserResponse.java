package com.creatorops.auth.dto;

import com.creatorops.auth.entity.User;

public record UserResponse(
    Long id,
    String name,
    String email,
    String imageUrl,
    Long organizationId
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getImageUrl(),
            user.getOrganizationId()
        );
    }
}
