package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class AssignmentStatusChangedEvent extends DomainEvent {
    public AssignmentStatusChangedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String oldStatus, String newStatus) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.ASSIGNMENT_STATUS_CHANGED,
            EntityType.ASSIGNMENT,
            "Assignment status changed from " + oldStatus + " to " + newStatus,
            "{\"oldStatus\":\"" + oldStatus + "\",\"newStatus\":\"" + newStatus + "\"}"
        );
    }
}
