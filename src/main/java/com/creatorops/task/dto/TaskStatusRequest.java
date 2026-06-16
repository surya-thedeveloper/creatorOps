package com.creatorops.task.dto;

import com.creatorops.task.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for updating the status of a Task.
 */
public record TaskStatusRequest(
    @NotNull(message = "Status is required")
    TaskStatus status
) {}
