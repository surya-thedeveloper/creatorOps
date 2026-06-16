package com.creatorops.analytics.dto;

import com.creatorops.assignment.entity.AssignmentStatus;
import com.creatorops.assignment.entity.AssignmentType;
import java.util.Map;

/**
 * <h3>AssignmentAnalyticsResponse</h3>
 * DTO record containing grouped count metrics for Assignments.
 * <p>
 * <h3>Why no analytics table was created</h3>
 * Assignment counts change dynamically as team members start and complete tasks. Avoiding a database analytics
 * table removes transactional locks, write contention, and synchronization latency from collaboration flows.
 */
public record AssignmentAnalyticsResponse(
    Map<AssignmentStatus, Long> assignmentsByStatus,
    Map<AssignmentType, Long> assignmentsByType
) {}
