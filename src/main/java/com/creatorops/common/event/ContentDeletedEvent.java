package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class ContentDeletedEvent extends DomainEvent {
    public ContentDeletedEvent(Long userId, Long organizationId, Long contentId, String title) {
        super(
            userId,
            organizationId,
            contentId,
            contentId,
            EventType.CONTENT_DELETED,
            EntityType.CONTENT,
            "Content '" + title + "' was deleted",
            null
        );
    }
}
