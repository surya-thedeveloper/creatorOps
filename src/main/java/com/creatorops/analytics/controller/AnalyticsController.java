package com.creatorops.analytics.controller;

import com.creatorops.analytics.dto.*;
import com.creatorops.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h3>AnalyticsController</h3>
 * REST Controller exposing read-only endpoints for CreatorOps operational dashboards and metrics.
 * All endpoints are secured by Spring Method Security and scoped to the caller's organization.
 * <p>
 * <h3>Why analytics is projection-based</h3>
 * Using a projection model allows endpoints to execute count aggregates on demand.
 * This guarantees the response payload is lightweight, up-to-date, and requires no synchronization locks.
 * <p>
 * <h3>Permissions and Role-Based Access Control</h3>
 * ADMIN, MANAGER, and CONTRIBUTOR roles are authorized to access these endpoints for V1.
 * Tenant isolation restricts the data returned to the caller's organization context.
 * <p>
 * <h3>Future Extension Points</h3>
 * If role-based restrictions are introduced in the future (e.g. limiting CONTRIBUTOR access to only a subset
 * of metrics), custom checks can be added using {@code @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")} on specific
 * endpoints, or role checks can be evaluated inside the service implementations.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CONTRIBUTOR')")
@Tag(name = "Analytics", description = "Read-only operational dashboard metrics. All metrics are scoped to the caller's organization.")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /api/analytics/dashboard
     * Returns operational summary metrics for the homepage.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard summary", description = "Returns top-level KPIs: total content, assignments, tasks, and active brands.")
    @ApiResponse(responseCode = "200", description = "Dashboard summary returned")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(Authentication authentication) {
        DashboardSummaryResponse response = analyticsService.getDashboardSummary(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/analytics/content
     * Returns Content grouping counts (Stage, Type, Priority).
     */
    @GetMapping("/content")
    @Operation(summary = "Content analytics", description = "Content breakdown by stage, type, and priority.")
    @ApiResponse(responseCode = "200", description = "Content analytics returned")
    public ResponseEntity<ContentAnalyticsResponse> getContentAnalytics(Authentication authentication) {
        ContentAnalyticsResponse response = analyticsService.getContentAnalytics(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/analytics/assignments
     * Returns Assignment grouping counts (Status, Type).
     */
    @GetMapping("/assignments")
    @Operation(summary = "Assignment analytics", description = "Assignment breakdown by status.")
    @ApiResponse(responseCode = "200", description = "Assignment analytics returned")
    public ResponseEntity<AssignmentAnalyticsResponse> getAssignmentAnalytics(Authentication authentication) {
        AssignmentAnalyticsResponse response = analyticsService.getAssignmentAnalytics(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/analytics/tasks
     * Returns Task grouping counts (Status, Priority) and overdue tasks.
     */
    @GetMapping("/tasks")
    @Operation(summary = "Task analytics", description = "Task breakdown by status and priority. Includes overdue task count.")
    @ApiResponse(responseCode = "200", description = "Task analytics returned")
    public ResponseEntity<TaskAnalyticsResponse> getTaskAnalytics(Authentication authentication) {
        TaskAnalyticsResponse response = analyticsService.getTaskAnalytics(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/analytics/publishing
     * Returns temporal publishing pipeline metrics.
     */
    @GetMapping("/publishing")
    @Operation(summary = "Publishing analytics", description = "Publishing pipeline metrics: this week, this month, overdue.")
    @ApiResponse(responseCode = "200", description = "Publishing analytics returned")
    public ResponseEntity<PublishingAnalyticsResponse> getPublishingAnalytics(Authentication authentication) {
        PublishingAnalyticsResponse response = analyticsService.getPublishingAnalytics(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
