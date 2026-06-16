package com.creatorops.activity.dto;

import com.creatorops.activity.entity.Activity;
import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;
import java.time.OffsetDateTime;

/**
 * DTO record mapping the properties of the Activity entity for API responses.
 */
public record ActivityResponse(
    Long id,
    Long contentId,
    Long userId,
    String userName,
    EventType eventType,
    EntityType entityType,
    Long entityId,
    String description,
    String metadataJson,
    OffsetDateTime createdAt
) {
    public static ActivityResponse fromEntity(Activity activity) {
        return new ActivityResponse(
            activity.getId(),
            activity.getContent().getId(),
            activity.getUser().getId(),
            activity.getUser().getName(),
            activity.getEventType(),
            activity.getEntityType(),
            activity.getEntityId(),
            activity.getDescription(),
            activity.getMetadataJson(),
            activity.getCreatedAt()
        );
    }
}
