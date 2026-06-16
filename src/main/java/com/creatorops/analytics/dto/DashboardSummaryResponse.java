package com.creatorops.analytics.dto;

/**
 * <h3>DashboardSummaryResponse</h3>
 * DTO record holding key operational dashboard metrics for the homepage.
 * <p>
 * <h3>Why analytics is projection-based</h3>
 * Analytics in CreatorOps is a read-only projection layer derived from existing entities
 * (Content, Assignment, Task, Asset). Constructing a read-only projection avoids storing redundant state,
 * eliminating synchronization errors, database bloat, and write overhead.
 * <p>
 * <h3>Why no analytics table was created</h3>
 * By querying the primary tables directly via database aggregates, the system maintains a single source of truth.
 * Storing aggregated counts in a separate table would require complex triggers, messaging queues, or cron jobs
 * to update, introducing latency and consistency issues.
 * <p>
 * <h3>How dashboards are generated efficiently</h3>
 * Projections use JPA/JPQL database-level aggregate counts (`COUNT`, `SUM`, `GROUP BY`) rather than fetching
 * entire entities into JVM memory. This minimizes database I/O, network payloads, and memory footprints.
 */
public record DashboardSummaryResponse(
    long totalContent,
    long scheduledContent,
    long publishedContent,
    long overdueContent,
    long totalAssignments,
    long activeAssignments,
    long totalTasks,
    long completedTasks,
    long overdueTasks,
    long totalAssets
) {}
