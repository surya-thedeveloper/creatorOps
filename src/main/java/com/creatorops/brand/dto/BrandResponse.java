package com.creatorops.brand.dto;

import com.creatorops.brand.entity.Brand;

public record BrandResponse(
    Long id,
    String name,
    String description,
    String logoUrl,
    Long organizationId
) {
    public static BrandResponse fromEntity(Brand brand) {
        return new BrandResponse(
            brand.getId(),
            brand.getName(),
            brand.getDescription(),
            brand.getLogoUrl(),
            brand.getOrganizationId()
        );
    }
}
