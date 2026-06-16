package com.creatorops.assignment.dto;

import com.creatorops.assignment.entity.AssignmentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * <h3>Why this class exists</h3>
 * {@code AssignmentStatusRequest} captures partial updates modifying only the assignment status.
 * <p>
 * <h3>How it supports creator collaboration</h3>
 * Allows contributors to update their progress (e.g. from ASSIGNED to IN_PROGRESS or BLOCKED) via lightweight endpoints.
 */
public record AssignmentStatusRequest(
    @NotNull(message = "Status is required")
    AssignmentStatus status
) {}
