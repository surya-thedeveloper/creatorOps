package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class ResearchUpdatedEvent extends DomainEvent {
    public ResearchUpdatedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String title) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.RESEARCH_UPDATED,
            EntityType.RESEARCH,
            "Research item '" + title + "' was updated",
            null
        );
    }
}
