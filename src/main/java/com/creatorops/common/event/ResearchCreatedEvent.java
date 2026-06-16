package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class ResearchCreatedEvent extends DomainEvent {
    public ResearchCreatedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String title) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.RESEARCH_CREATED,
            EntityType.RESEARCH,
            "Research item '" + title + "' was added",
            null
        );
    }
}
