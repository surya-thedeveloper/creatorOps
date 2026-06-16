package com.creatorops.task.dto;

import com.creatorops.task.entity.Task;
import com.creatorops.task.entity.TaskPriority;
import com.creatorops.task.entity.TaskStatus;
import java.time.OffsetDateTime;

/**
 * Response payload representing a Task.
 */
public record TaskResponse(
    Long id,
    Long assignmentId,
    String assignmentType,
    Long contentId,
    String contentTitle,
    Long assignedToUserId,
    String assignedToUserName,
    Long createdByUserId,
    String createdByUserName,
    String title,
    String description,
    TaskStatus status,
    TaskPriority priority,
    OffsetDateTime dueDate,
    OffsetDateTime completedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static TaskResponse fromEntity(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getAssignment().getId(),
            task.getAssignment().getAssignmentType().name(),
            task.getAssignment().getContent().getId(),
            task.getAssignment().getContent().getTitle(),
            task.getAssignedToUser().getId(),
            task.getAssignedToUser().getName(),
            task.getCreatedByUser().getId(),
            task.getCreatedByUser().getName(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            task.getDueDate(),
            task.getCompletedAt(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
