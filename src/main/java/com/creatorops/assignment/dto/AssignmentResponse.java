package com.creatorops.assignment.dto;

import com.creatorops.assignment.entity.Assignment;
import com.creatorops.assignment.entity.AssignmentStatus;
import com.creatorops.assignment.entity.AssignmentType;
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code AssignmentResponse} maps persistent assignment records to serialized HTTP output bodies.
 * <p>
 * <h3>Relationship Design</h3>
 * Integrates a static {@code fromEntity} converter transforming JPA objects to records.
 * <p>
 * <h3>How it supports creator collaboration</h3>
 * Serializes ownership details (assignee and assigner names/IDs), progress states, and timelines for client dashboard displays.
 */
public record AssignmentResponse(
    Long id,
    Long contentId,
    Long assignedToUserId,
    String assignedToUserName,
    Long assignedByUserId,
    String assignedByUserName,
    AssignmentType assignmentType,
    AssignmentStatus status,
    String notes,
    OffsetDateTime dueDate,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static AssignmentResponse fromEntity(Assignment entity) {
        if (entity == null) {
            return null;
        }
        return new AssignmentResponse(
            entity.getId(),
            entity.getContentId(),
            entity.getAssignedToUser() != null ? entity.getAssignedToUser().getId() : null,
            entity.getAssignedToUser() != null ? entity.getAssignedToUser().getName() : null,
            entity.getAssignedByUser() != null ? entity.getAssignedByUser().getId() : null,
            entity.getAssignedByUser() != null ? entity.getAssignedByUser().getName() : null,
            entity.getAssignmentType(),
            entity.getStatus(),
            entity.getNotes(),
            entity.getDueDate(),
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
