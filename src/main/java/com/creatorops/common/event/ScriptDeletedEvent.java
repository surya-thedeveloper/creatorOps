package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class ScriptDeletedEvent extends DomainEvent {
    public ScriptDeletedEvent(Long userId, Long organizationId, Long contentId, Long entityId, Integer version) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.SCRIPT_DELETED,
            EntityType.SCRIPT,
            "Script version " + version + " deleted",
            null
        );
    }
}
