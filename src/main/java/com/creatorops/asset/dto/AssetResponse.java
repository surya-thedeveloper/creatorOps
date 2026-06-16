package com.creatorops.asset.dto;

import com.creatorops.asset.entity.Asset;
import com.creatorops.asset.entity.AssetSource;
import com.creatorops.asset.entity.AssetType;
import java.time.OffsetDateTime;

/**
 * Response payload representing an Asset.
 */
public record AssetResponse(
    Long id,
    Long contentId,
    String contentTitle,
    Long uploadedByUserId,
    String uploadedByUserName,
    AssetType assetType,
    AssetSource assetSource,
    String name,
    String description,
    String fileUrl,
    Long fileSize,
    String mimeType,
    Integer version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static AssetResponse fromEntity(Asset asset) {
        return new AssetResponse(
            asset.getId(),
            asset.getContent().getId(),
            asset.getContent().getTitle(),
            asset.getUploadedByUser().getId(),
            asset.getUploadedByUser().getName(),
            asset.getAssetType(),
            asset.getAssetSource(),
            asset.getName(),
            asset.getDescription(),
            asset.getFileUrl(),
            asset.getFileSize(),
            asset.getMimeType(),
            asset.getVersion(),
            asset.getCreatedAt(),
            asset.getUpdatedAt()
        );
    }
}
