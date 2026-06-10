package com.creatorops.organization.dto;

import com.creatorops.organization.entity.Organization;
import java.time.OffsetDateTime;

public record OrganizationResponse(
    Long id,
    String name,
    String logoUrl,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static OrganizationResponse fromEntity(Organization organization) {
        return new OrganizationResponse(
            organization.getId(),
            organization.getName(),
            organization.getLogoUrl(),
            organization.getCreatedAt(),
            organization.getUpdatedAt()
        );
    }
}
