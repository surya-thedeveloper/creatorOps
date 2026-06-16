package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class ContentUpdatedEvent extends DomainEvent {
    public ContentUpdatedEvent(Long userId, Long organizationId, Long contentId, String title, EventType eventType, String description) {
        super(
            userId,
            organizationId,
            contentId,
            contentId,
            eventType,
            EntityType.CONTENT,
            description,
            null
        );
    }
}
