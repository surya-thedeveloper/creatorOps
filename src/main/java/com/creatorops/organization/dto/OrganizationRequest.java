package com.creatorops.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationRequest(
    @NotBlank(message = "Organization name is required")
    @Size(max = 255, message = "Organization name must be less than 255 characters")
    String name,

    @Size(max = 1024, message = "Logo URL must be less than 1024 characters")
    String logoUrl
) {}
