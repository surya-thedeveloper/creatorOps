package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class TaskDeletedEvent extends DomainEvent {
    public TaskDeletedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String title) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.TASK_DELETED,
            EntityType.TASK,
            "Task '" + title + "' was deleted",
            null
        );
    }
}
