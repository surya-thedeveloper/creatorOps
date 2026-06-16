package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class TaskStatusChangedEvent extends DomainEvent {
    public TaskStatusChangedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String oldStatus, String newStatus) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.TASK_STATUS_CHANGED,
            EntityType.TASK,
            "Task status changed from " + oldStatus + " to " + newStatus,
            "{\"oldStatus\":\"" + oldStatus + "\",\"newStatus\":\"" + newStatus + "\"}"
        );
    }
}
