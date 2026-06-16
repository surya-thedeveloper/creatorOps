package com.creatorops.analytics.service;

import com.creatorops.analytics.dto.*;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.content.repository.ContentRepository;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.entity.ContentPriority;
import com.creatorops.assignment.repository.AssignmentRepository;
import com.creatorops.assignment.entity.AssignmentStatus;
import com.creatorops.assignment.entity.AssignmentType;
import com.creatorops.task.repository.TaskRepository;
import com.creatorops.task.entity.TaskStatus;
import com.creatorops.task.entity.TaskPriority;
import com.creatorops.asset.repository.AssetRepository;
import com.creatorops.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.Map;

/**
 * <h3>AnalyticsServiceImpl</h3>
 * Implements read-only projection calculations for operational dashboard reporting.
 * <p>
 * <h3>Why analytics is projection-based</h3>
 * Analytics in CreatorOps is modeled as a read-only projection layer rather than a separate database table schema.
 * Creating a database table for analytics would introduce redundant state, data synchronization latency,
 * and concurrency issues during frequent updates. Direct projections ensure real-time consistency.
 * <p>
 * <h3>Why no analytics table was created</h3>
 * Since analytics are derived solely from existing Content, Assignment, Task, and Asset records,
 * the primary tables remain the single source of truth. Querying counts directly eliminates standard
 * cache invalidation challenges.
 * <p>
 * <h3>How aggregate queries work</h3>
 * Standard JPA/JPQL repository queries count items directly in the database (`COUNT`, `GROUP BY`) rather than
 * loading heavy entity graphs into JVM memory. For grouping queries, they return `List<Object[]>` containing key-count
 * tuples which are mapped inside the service layer.
 * <p>
 * <h3>How tenant isolation is enforced</h3>
 * Enforced at the service boundary: every transaction retrieves the requesting user's profile based on their
 * security context authentication credentials (email), extracts their active `organizationId`, and passes it to the
 * repository query filters. Data belonging to other organizations is never accessed.
 * <p>
 * <h3>Future Extension Points</h3>
 * This module is architected to support future requirements:
 * <ul>
 *   <li><b>Team workload analytics</b>: Can join assignments and task records grouped by `assignedToUser.id` to balance resources.</li>
 *   <li><b>Productivity analytics & publishing velocity</b>: Can map activity log transition timestamps to determine planning-to-publish lead times.</li>
 *   <li><b>Creator performance metrics</b>: Can compare completed assignment deadlines vs overdue assignments per creator user.</li>
 *   <li><b>Custom reports & CSV/Excel exports</b>: Can write custom controller media mapping outputs utilizing libraries like Apache POI or OpenCSV.</li>
 * </ul>
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final AssignmentRepository assignmentRepository;
    private final TaskRepository taskRepository;
    private final AssetRepository assetRepository;

    @Autowired
    public AnalyticsServiceImpl(UserRepository userRepository,
                                ContentRepository contentRepository,
                                AssignmentRepository assignmentRepository,
                                TaskRepository taskRepository,
                                AssetRepository assetRepository) {
        this.userRepository = userRepository;
        this.contentRepository = contentRepository;
        this.assignmentRepository = assignmentRepository;
        this.taskRepository = taskRepository;
        this.assetRepository = assetRepository;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(String currentUserEmail) {
        User user = getUser(currentUserEmail);
        Long orgId = user.getOrganizationId();
        OffsetDateTime now = OffsetDateTime.now();

        long totalContent = contentRepository.countByOrganizationId(orgId);
        long scheduledContent = contentRepository.countScheduledByOrganizationId(orgId);
        long publishedContent = contentRepository.countPublishedByOrganizationId(orgId);
        long overdueContent = contentRepository.countOverdueByOrganizationId(orgId, now);

        long totalAssignments = assignmentRepository.countByOrganizationId(orgId);
        long activeAssignments = assignmentRepository.countActiveByOrganizationId(orgId);

        long totalTasks = taskRepository.countByOrganizationId(orgId);
        long completedTasks = taskRepository.countCompletedByOrganizationId(orgId);
        long overdueTasks = taskRepository.countOverdueByOrganizationId(orgId, now);

        long totalAssets = assetRepository.countByOrganizationId(orgId);

        return new DashboardSummaryResponse(
                totalContent,
                scheduledContent,
                publishedContent,
                overdueContent,
                totalAssignments,
                activeAssignments,
                totalTasks,
                completedTasks,
                overdueTasks,
                totalAssets
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ContentAnalyticsResponse getContentAnalytics(String currentUserEmail) {
        User user = getUser(currentUserEmail);
        Long orgId = user.getOrganizationId();

        // 1. Stage Map
        Map<ContentStage, Long> stageMap = new EnumMap<>(ContentStage.class);
        for (ContentStage stage : ContentStage.values()) {
            stageMap.put(stage, 0L);
        }
        for (Object[] row : contentRepository.countByStage(orgId)) {
            if (row[0] != null) {
                ContentStage stage = row[0] instanceof ContentStage ? (ContentStage) row[0] : ContentStage.valueOf(row[0].toString());
                stageMap.put(stage, (Long) row[1]);
            }
        }

        // 2. Type Map
        Map<ContentType, Long> typeMap = new EnumMap<>(ContentType.class);
        for (ContentType type : ContentType.values()) {
            typeMap.put(type, 0L);
        }
        for (Object[] row : contentRepository.countByType(orgId)) {
            if (row[0] != null) {
                ContentType type = row[0] instanceof ContentType ? (ContentType) row[0] : ContentType.valueOf(row[0].toString());
                typeMap.put(type, (Long) row[1]);
            }
        }

        // 3. Priority Map
        Map<ContentPriority, Long> priorityMap = new EnumMap<>(ContentPriority.class);
        for (ContentPriority priority : ContentPriority.values()) {
            priorityMap.put(priority, 0L);
        }
        for (Object[] row : contentRepository.countByPriority(orgId)) {
            if (row[0] != null) {
                ContentPriority priority = row[0] instanceof ContentPriority ? (ContentPriority) row[0] : ContentPriority.valueOf(row[0].toString());
                priorityMap.put(priority, (Long) row[1]);
            }
        }

        return new ContentAnalyticsResponse(stageMap, typeMap, priorityMap);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentAnalyticsResponse getAssignmentAnalytics(String currentUserEmail) {
        User user = getUser(currentUserEmail);
        Long orgId = user.getOrganizationId();

        // 1. Status Map
        Map<AssignmentStatus, Long> statusMap = new EnumMap<>(AssignmentStatus.class);
        for (AssignmentStatus status : AssignmentStatus.values()) {
            statusMap.put(status, 0L);
        }
        for (Object[] row : assignmentRepository.countByStatus(orgId)) {
            if (row[0] != null) {
                AssignmentStatus status = row[0] instanceof AssignmentStatus ? (AssignmentStatus) row[0] : AssignmentStatus.valueOf(row[0].toString());
                statusMap.put(status, (Long) row[1]);
            }
        }

        // 2. Type Map
        Map<AssignmentType, Long> typeMap = new EnumMap<>(AssignmentType.class);
        for (AssignmentType type : AssignmentType.values()) {
            typeMap.put(type, 0L);
        }
        for (Object[] row : assignmentRepository.countByType(orgId)) {
            if (row[0] != null) {
                AssignmentType type = row[0] instanceof AssignmentType ? (AssignmentType) row[0] : AssignmentType.valueOf(row[0].toString());
                typeMap.put(type, (Long) row[1]);
            }
        }

        return new AssignmentAnalyticsResponse(statusMap, typeMap);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskAnalyticsResponse getTaskAnalytics(String currentUserEmail) {
        User user = getUser(currentUserEmail);
        Long orgId = user.getOrganizationId();
        OffsetDateTime now = OffsetDateTime.now();

        // 1. Status Map
        Map<TaskStatus, Long> statusMap = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            statusMap.put(status, 0L);
        }
        for (Object[] row : taskRepository.countByStatus(orgId)) {
            if (row[0] != null) {
                TaskStatus status = row[0] instanceof TaskStatus ? (TaskStatus) row[0] : TaskStatus.valueOf(row[0].toString());
                statusMap.put(status, (Long) row[1]);
            }
        }

        // 2. Priority Map
        Map<TaskPriority, Long> priorityMap = new EnumMap<>(TaskPriority.class);
        for (TaskPriority priority : TaskPriority.values()) {
            priorityMap.put(priority, 0L);
        }
        for (Object[] row : taskRepository.countByPriority(orgId)) {
            if (row[0] != null) {
                TaskPriority priority = row[0] instanceof TaskPriority ? (TaskPriority) row[0] : TaskPriority.valueOf(row[0].toString());
                priorityMap.put(priority, (Long) row[1]);
            }
        }

        long overdueTasks = taskRepository.countOverdueByOrganizationId(orgId, now);

        return new TaskAnalyticsResponse(statusMap, priorityMap, overdueTasks);
    }

    @Override
    @Transactional(readOnly = true)
    public PublishingAnalyticsResponse getPublishingAnalytics(String currentUserEmail) {
        User user = getUser(currentUserEmail);
        Long orgId = user.getOrganizationId();
        OffsetDateTime now = OffsetDateTime.now();

        // Weekly bounds (Monday to Sunday)
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        OffsetDateTime startOfWeek = now.minusDays(dayOfWeek.getValue() - 1).with(LocalTime.MIN);
        OffsetDateTime endOfWeek = startOfWeek.plusDays(6).with(LocalTime.MAX);

        // Monthly bounds
        OffsetDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        OffsetDateTime endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        long publishedThisWeek = contentRepository.countByStageAndPublishDateRange(orgId, ContentStage.PUBLISHED, startOfWeek, endOfWeek);
        long publishedThisMonth = contentRepository.countByStageAndPublishDateRange(orgId, ContentStage.PUBLISHED, startOfMonth, endOfMonth);

        long upcomingThisWeek = contentRepository.countByPublishDateRange(orgId, now, endOfWeek);
        long upcomingThisMonth = contentRepository.countByPublishDateRange(orgId, now, endOfMonth);

        long scheduledThisWeek = contentRepository.countByStageAndPublishDateRange(orgId, ContentStage.SCHEDULED, startOfWeek, endOfWeek);
        long scheduledThisMonth = contentRepository.countByStageAndPublishDateRange(orgId, ContentStage.SCHEDULED, startOfMonth, endOfMonth);

        return new PublishingAnalyticsResponse(
                publishedThisWeek,
                publishedThisMonth,
                upcomingThisWeek,
                upcomingThisMonth,
                scheduledThisWeek,
                scheduledThisMonth
        );
    }
}
