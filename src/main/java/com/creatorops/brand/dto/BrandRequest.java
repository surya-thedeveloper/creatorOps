package com.creatorops.brand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequest(
    @NotBlank(message = "Brand name is required")
    @Size(max = 255, message = "Brand name cannot exceed 255 characters")
    String name,

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    String description,

    @Size(max = 1024, message = "Logo URL cannot exceed 1024 characters")
    String logoUrl
) {}
