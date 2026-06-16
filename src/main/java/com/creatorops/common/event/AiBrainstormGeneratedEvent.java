package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class AiBrainstormGeneratedEvent extends DomainEvent {
    public AiBrainstormGeneratedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String title) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.AI_BRAINSTORM_GENERATED,
            EntityType.RESEARCH,
            "AI brainstorm generated: title=" + title,
            null
        );
    }
}
