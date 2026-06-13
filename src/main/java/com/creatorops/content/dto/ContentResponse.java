package com.creatorops.content.dto;

import com.creatorops.content.entity.Content;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.entity.ContentPriority;
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code ContentResponse} serves as the response DTO wrapper. It formats content properties returned from the server.
 * <p>
 * <h3>Why it belongs in this package</h3>
 * Placed in {@code com.creatorops.content.dto} to separate output models from internal entity schemas.
 * <p>
 * <h3>Key Annotations</h3>
 * Standard Java record.
 * <p>
 * <h3>Design Decisions</h3>
 * <ul>
 *   <li>Exposes standard response parameters matching the API Design Specification.</li>
 *   <li>Uses a static factory mapper method {@code fromEntity} to map entities cleanly without heavy mapping frameworks.</li>
 * </ul>
 */
public record ContentResponse(
    Long id,
    Long brandId,
    String title,
    String description,
    ContentType type,
    ContentStage stage,
    ContentPriority priority,
    OffsetDateTime dueDate,
    OffsetDateTime publishDate,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ContentResponse fromEntity(Content content) {
        return new ContentResponse(
            content.getId(),
            content.getBrandId(),
            content.getTitle(),
            content.getDescription(),
            content.getType(),
            content.getStage(),
            content.getPriority(),
            content.getDueDate(),
            content.getPublishDate(),
            content.getCreatedAt(),
            content.getUpdatedAt()
        );
    }
}
