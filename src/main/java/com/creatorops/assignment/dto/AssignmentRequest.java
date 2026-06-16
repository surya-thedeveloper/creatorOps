package com.creatorops.assignment.dto;

import com.creatorops.assignment.entity.AssignmentType;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code AssignmentRequest} models the payload needed to allocate or re-allocate assignments.
 * <p>
 * <h3>How it supports creator collaboration</h3>
 * Captures user choices (assignee, type, guidelines notes, target due date) from creator workspace screens.
 */
public record AssignmentRequest(
    @NotNull(message = "Assigned user ID is required")
    Long assignedToUserId,

    @NotNull(message = "Assignment type is required")
    AssignmentType assignmentType,

    String notes,
    OffsetDateTime dueDate
) {}
