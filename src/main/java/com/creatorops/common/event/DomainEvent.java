package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Abstract base class for all business domain events in the application.
 */
public abstract class DomainEvent {

    private final String eventId;
    private final OffsetDateTime occurredAt;
    private final Long userId;
    private final Long organizationId;
    private final Long contentId;
    private final Long entityId;
    private final EventType eventType;
    private final EntityType entityType;
    private final String description;
    private final String metadataJson;

    protected DomainEvent(Long userId, Long organizationId, Long contentId, Long entityId,
                          EventType eventType, EntityType entityType, String description, String metadataJson) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = OffsetDateTime.now();
        this.userId = userId;
        this.organizationId = organizationId;
        this.contentId = contentId;
        this.entityId = entityId;
        this.eventType = eventType;
        this.entityType = entityType;
        this.description = description;
        this.metadataJson = metadataJson;
    }

    public String getEventId() {
        return eventId;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getContentId() {
        return contentId;
    }

    public Long getEntityId() {
        return entityId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getDescription() {
        return description;
    }

    public String getMetadataJson() {
        return metadataJson;
    }
}
