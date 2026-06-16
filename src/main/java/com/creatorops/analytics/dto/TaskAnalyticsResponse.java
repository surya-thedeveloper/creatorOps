package com.creatorops.analytics.dto;

import com.creatorops.task.entity.TaskStatus;
import com.creatorops.task.entity.TaskPriority;
import java.util.Map;

/**
 * <h3>TaskAnalyticsResponse</h3>
 * DTO record containing grouped count metrics and overdue frequencies for checklist Tasks.
 * <p>
 * <h3>How tenant isolation is enforced</h3>
 * For all aggregates, the repository queries join through the parent relationship chain to verify that the
 * tasks reside in content cards owned by the user's specific organization.
 */
public record TaskAnalyticsResponse(
    Map<TaskStatus, Long> tasksByStatus,
    Map<TaskPriority, Long> tasksByPriority,
    long overdueTasks
) {}
