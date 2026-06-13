package com.creatorops.research.dto;

import com.creatorops.research.entity.ResearchItem;
import com.creatorops.research.entity.ResearchItemType;
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code ResearchItemResponse} serves as the JSON output model returned by endpoints.
 * <p>
 * <h3>Relationship Design</h3>
 * Uses a static factory mapping {@code fromEntity} to convert core entities to serializable records.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Provides structured details back to frontend UI workspaces, matching API spec outlines.
 */
public record ResearchItemResponse(
    Long id,
    Long contentId,
    Long userId,
    ResearchItemType type,
    String title,
    String content,
    String externalUrl,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ResearchItemResponse fromEntity(ResearchItem item) {
        return new ResearchItemResponse(
            item.getId(),
            item.getContentId(),
            item.getUserId(),
            item.getType(),
            item.getTitle(),
            item.getContentText(),
            item.getUrl(), // Maps external_url to url in DB
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }
}
