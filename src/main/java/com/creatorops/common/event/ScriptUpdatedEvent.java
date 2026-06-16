package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class ScriptUpdatedEvent extends DomainEvent {
    public ScriptUpdatedEvent(Long userId, Long organizationId, Long contentId, Long entityId, Integer version) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.SCRIPT_UPDATED,
            EntityType.SCRIPT,
            "Script version " + version + " updated",
            null
        );
    }
}
