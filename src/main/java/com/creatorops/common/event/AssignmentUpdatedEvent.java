package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class AssignmentUpdatedEvent extends DomainEvent {
    public AssignmentUpdatedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String typeName, String assigneeName) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.ASSIGNMENT_UPDATED,
            EntityType.ASSIGNMENT,
            "Assignment for " + typeName + " assigned to " + assigneeName + " updated",
            null
        );
    }
}
