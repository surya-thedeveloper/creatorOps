package com.creatorops.common.event;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;

public class AssignmentCreatedEvent extends DomainEvent {
    public AssignmentCreatedEvent(Long userId, Long organizationId, Long contentId, Long entityId, String typeName, String assigneeName) {
        super(
            userId,
            organizationId,
            contentId,
            entityId,
            EventType.ASSIGNMENT_CREATED,
            EntityType.ASSIGNMENT,
            "Assignment for " + typeName + " assigned to " + assigneeName + " created",
            null
        );
    }
}
