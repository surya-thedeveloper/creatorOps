package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class ScriptCreatedEvent extends DomainEvent {
    public ScriptCreatedEvent(Long userId, Long organizationId, Long contentId, Long entityId, Integer version) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.SCRIPT_CREATED,
            EntityType.SCRIPT,
            "Script version " + version + " created",
            null
        );
    }
}
