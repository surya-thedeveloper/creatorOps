package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class ResearchDeletedEvent extends DomainEvent {
    public ResearchDeletedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String title) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.RESEARCH_DELETED,
            EntityType.RESEARCH,
            "Research item '" + title + "' was deleted",
            null
        );
    }
}
