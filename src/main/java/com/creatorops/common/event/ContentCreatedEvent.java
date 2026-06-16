package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class ContentCreatedEvent extends DomainEvent {
    public ContentCreatedEvent(Long userId, Long organizationId, Long contentId, String title, String stageName) {
        super(
            userId,
            organizationId,
            contentId,
            contentId,
            EventType.CONTENT_CREATED,
            EntityType.CONTENT,
            "Content '" + title + "' was created",
            stageName != null ? "{\"stage\":\"" + stageName + "\"}" : null
        );
    }
}
