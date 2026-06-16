package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class TaskUpdatedEvent extends DomainEvent {
    public TaskUpdatedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String title) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.TASK_UPDATED,
            EntityType.TASK,
            "Task '" + title + "' was updated",
            null
        );
    }
}
