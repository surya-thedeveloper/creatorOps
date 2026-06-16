package com.creatorops.asset.dto;

import com.creatorops.asset.entity.AssetSource;
import com.creatorops.asset.entity.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for creating and updating an Asset.
 */
public record AssetRequest(
    @NotBlank(message = "Asset name is required")
    String name,

    String description,

    @NotNull(message = "Asset type is required")
    AssetType assetType,

    @NotNull(message = "Asset source is required")
    AssetSource assetSource,

    @NotBlank(message = "File URL is required")
    String fileUrl,

    Long fileSize,

    String mimeType,

    Integer version
) {}
