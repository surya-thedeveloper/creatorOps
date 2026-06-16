package com.creatorops.assignment.entity;

/**
 * <h3>Why this class exists</h3>
 * {@code AssignmentStatus} lists the state lifecycle steps of an execution assignment on a Content planning card.
 * <p>
 * <h3>How it supports creator collaboration</h3>
 * Signals work status (e.g. BLOCKED, IN_PROGRESS) to other team members, keeping the production pipeline visible.
 */
public enum AssignmentStatus {
    ASSIGNED,
    IN_PROGRESS,
    BLOCKED,
    COMPLETED,
    CANCELLED
}
