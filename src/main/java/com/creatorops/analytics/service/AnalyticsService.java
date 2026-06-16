package com.creatorops.analytics.service;

import com.creatorops.analytics.dto.*;

/**
 * <h3>AnalyticsService</h3>
 * Service interface specifying the read-only projection methods for dashboard analytics.
 * <p>
 * <h3>Why analytics is projection-based</h3>
 * Using projection-based read models avoids creating new tables for tracking analytics.
 * The data is queried directly from the source tables on demand, preventing stale data and reducing storage.
 * <p>
 * <h3>How tenant isolation is enforced</h3>
 * Enforced in the service layer: the logged-in user's email is used to retrieve their active organization,
 * and all aggregate queries are restricted to that organization ID.
 */
public interface AnalyticsService {

    /**
     * Retrieves high-level operational counts for the home dashboard.
     *
     * @param currentUserEmail Email of the authenticated user requesting data.
     * @return DashboardSummaryResponse containing core metrics.
     */
    DashboardSummaryResponse getDashboardSummary(String currentUserEmail);

    /**
     * Retrieves aggregated groupings for Content cards.
     *
     * @param currentUserEmail Email of the authenticated user requesting data.
     * @return ContentAnalyticsResponse containing content grouped by stage, type, and priority.
     */
    ContentAnalyticsResponse getContentAnalytics(String currentUserEmail);

    /**
     * Retrieves aggregated groupings for Assignments.
     *
     * @param currentUserEmail Email of the authenticated user requesting data.
     * @return AssignmentAnalyticsResponse containing assignments grouped by status and type.
     */
    AssignmentAnalyticsResponse getAssignmentAnalytics(String currentUserEmail);

    /**
     * Retrieves aggregated groupings and overdue metrics for checklist Tasks.
     *
     * @param currentUserEmail Email of the authenticated user requesting data.
     * @return TaskAnalyticsResponse containing tasks grouped by status and priority, and overdue task count.
     */
    TaskAnalyticsResponse getTaskAnalytics(String currentUserEmail);

    /**
     * Retrieves temporal metrics on published, upcoming, and scheduled content.
     *
     * @param currentUserEmail Email of the authenticated user requesting data.
     * @return PublishingAnalyticsResponse containing weekly and monthly publication pipeline scopes.
     */
    PublishingAnalyticsResponse getPublishingAnalytics(String currentUserEmail);
}
