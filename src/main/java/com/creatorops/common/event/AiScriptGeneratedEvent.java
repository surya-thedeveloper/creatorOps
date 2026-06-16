package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class AiScriptGeneratedEvent extends DomainEvent {
    public AiScriptGeneratedEvent(Long userId, Long organizationId, Long contentId, Long entityId, Integer version) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.AI_SCRIPT_GENERATED,
            EntityType.SCRIPT,
            "AI script draft generated: version=" + version,
            null
        );
    }
}
