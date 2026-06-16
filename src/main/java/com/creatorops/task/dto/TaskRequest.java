package com.creatorops.task.dto;

import com.creatorops.task.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * Request payload for creating and updating a Task.
 */
public record TaskRequest(
    @NotBlank(message = "Title is required")
    String title,

    String description,

    @NotNull(message = "Priority is required")
    TaskPriority priority,

    @NotNull(message = "Assigned user ID is required")
    Long assignedToUserId,

    OffsetDateTime dueDate
) {}
