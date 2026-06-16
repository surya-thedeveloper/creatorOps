package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class TaskCreatedEvent extends DomainEvent {
    public TaskCreatedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String title) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.TASK_CREATED,
            EntityType.TASK,
            "Task '" + title + "' was created",
            null
        );
    }
}
