package com.creatorops.analytics;

import com.creatorops.analytics.dto.*;
import com.creatorops.analytics.service.AnalyticsServiceImpl;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.content.entity.ContentPriority;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.repository.ContentRepository;
import com.creatorops.assignment.entity.AssignmentStatus;
import com.creatorops.assignment.entity.AssignmentType;
import com.creatorops.assignment.repository.AssignmentRepository;
import com.creatorops.task.entity.TaskPriority;
import com.creatorops.task.entity.TaskStatus;
import com.creatorops.task.repository.TaskRepository;
import com.creatorops.asset.repository.AssetRepository;
import com.creatorops.organization.entity.Organization;
import com.creatorops.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private User user;
    private Organization organization;
    private final String userEmail = "tony@slay.com";
    private final Long orgId = 1L;

    @BeforeEach
    void setUp() {
        organization = new Organization("Slay Media", null);
        organization.setId(orgId);

        user = new User("Tony Admin", userEmail, "hashed_password");
        user.setId(2L);
        user.setRole(UserRole.ADMIN);
        user.setOrganization(organization);
    }

    @Test
    void getDashboardSummary_Success() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(contentRepository.countByOrganizationId(orgId)).thenReturn(10L);
        when(contentRepository.countScheduledByOrganizationId(orgId)).thenReturn(3L);
        when(contentRepository.countPublishedByOrganizationId(orgId)).thenReturn(4L);
        when(contentRepository.countOverdueByOrganizationId(eq(orgId), any(OffsetDateTime.class))).thenReturn(2L);

        when(assignmentRepository.countByOrganizationId(orgId)).thenReturn(8L);
        when(assignmentRepository.countActiveByOrganizationId(orgId)).thenReturn(5L);

        when(taskRepository.countByOrganizationId(orgId)).thenReturn(15L);
        when(taskRepository.countCompletedByOrganizationId(orgId)).thenReturn(10L);
        when(taskRepository.countOverdueByOrganizationId(eq(orgId), any(OffsetDateTime.class))).thenReturn(1L);

        when(assetRepository.countByOrganizationId(orgId)).thenReturn(12L);

        DashboardSummaryResponse response = analyticsService.getDashboardSummary(userEmail);

        assertNotNull(response);
        assertEquals(10, response.totalContent());
        assertEquals(3, response.scheduledContent());
        assertEquals(4, response.publishedContent());
        assertEquals(2, response.overdueContent());
        assertEquals(8, response.totalAssignments());
        assertEquals(5, response.activeAssignments());
        assertEquals(15, response.totalTasks());
        assertEquals(10, response.completedTasks());
        assertEquals(1, response.overdueTasks());
        assertEquals(12, response.totalAssets());
    }

    @Test
    void getContentAnalytics_Success() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        List<Object[]> mockStages = List.of(
                new Object[]{ContentStage.IDEA, 5L},
                new Object[]{ContentStage.PUBLISHED, 2L}
        );
        when(contentRepository.countByStage(orgId)).thenReturn(mockStages);

        List<Object[]> mockTypes = List.of(
                new Object[][]{{ContentType.YOUTUBE_VIDEO, 3L}}
        );
        when(contentRepository.countByType(orgId)).thenReturn(mockTypes);

        List<Object[]> mockPriorities = List.of(
                new Object[][]{{ContentPriority.HIGH, 4L}}
        );
        when(contentRepository.countByPriority(orgId)).thenReturn(mockPriorities);

        ContentAnalyticsResponse response = analyticsService.getContentAnalytics(userEmail);

        assertNotNull(response);
        assertEquals(5L, response.contentByStage().get(ContentStage.IDEA));
        assertEquals(2L, response.contentByStage().get(ContentStage.PUBLISHED));
        assertEquals(0L, response.contentByStage().get(ContentStage.RESEARCH)); // Defaults to 0

        assertEquals(3L, response.contentByType().get(ContentType.YOUTUBE_VIDEO));
        assertEquals(0L, response.contentByType().get(ContentType.BLOG));

        assertEquals(4L, response.contentByPriority().get(ContentPriority.HIGH));
        assertEquals(0L, response.contentByPriority().get(ContentPriority.LOW));
    }

    @Test
    void getContentAnalytics_EmptyDataset() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(contentRepository.countByStage(orgId)).thenReturn(Collections.emptyList());
        when(contentRepository.countByType(orgId)).thenReturn(Collections.emptyList());
        when(contentRepository.countByPriority(orgId)).thenReturn(Collections.emptyList());

        ContentAnalyticsResponse response = analyticsService.getContentAnalytics(userEmail);

        assertNotNull(response);
        assertNotNull(response.contentByStage());
        assertEquals(ContentStage.values().length, response.contentByStage().size());
        assertTrue(response.contentByStage().values().stream().allMatch(val -> val == 0L));

        assertNotNull(response.contentByType());
        assertEquals(ContentType.values().length, response.contentByType().size());
        assertTrue(response.contentByType().values().stream().allMatch(val -> val == 0L));

        assertNotNull(response.contentByPriority());
        assertEquals(ContentPriority.values().length, response.contentByPriority().size());
        assertTrue(response.contentByPriority().values().stream().allMatch(val -> val == 0L));
    }

    @Test
    void getAssignmentAnalytics_Success() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        List<Object[]> mockStatuses = List.of(
                new Object[][]{{AssignmentStatus.IN_PROGRESS, 3L}}
        );
        when(assignmentRepository.countByStatus(orgId)).thenReturn(mockStatuses);

        List<Object[]> mockTypes = List.of(
                new Object[][]{{AssignmentType.SCRIPT, 4L}}
        );
        when(assignmentRepository.countByType(orgId)).thenReturn(mockTypes);

        AssignmentAnalyticsResponse response = analyticsService.getAssignmentAnalytics(userEmail);

        assertNotNull(response);
        assertEquals(3L, response.assignmentsByStatus().get(AssignmentStatus.IN_PROGRESS));
        assertEquals(0L, response.assignmentsByStatus().get(AssignmentStatus.COMPLETED));

        assertEquals(4L, response.assignmentsByType().get(AssignmentType.SCRIPT));
        assertEquals(0L, response.assignmentsByType().get(AssignmentType.RESEARCH));
    }

    @Test
    void getAssignmentAnalytics_EmptyDataset() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(assignmentRepository.countByStatus(orgId)).thenReturn(Collections.emptyList());
        when(assignmentRepository.countByType(orgId)).thenReturn(Collections.emptyList());

        AssignmentAnalyticsResponse response = analyticsService.getAssignmentAnalytics(userEmail);

        assertNotNull(response);
        assertNotNull(response.assignmentsByStatus());
        assertEquals(AssignmentStatus.values().length, response.assignmentsByStatus().size());
        assertTrue(response.assignmentsByStatus().values().stream().allMatch(val -> val == 0L));

        assertNotNull(response.assignmentsByType());
        assertEquals(AssignmentType.values().length, response.assignmentsByType().size());
        assertTrue(response.assignmentsByType().values().stream().allMatch(val -> val == 0L));
    }

    @Test
    void getTaskAnalytics_Success() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        List<Object[]> mockStatuses = List.of(
                new Object[][]{{TaskStatus.TODO, 6L}}
        );
        when(taskRepository.countByStatus(orgId)).thenReturn(mockStatuses);

        List<Object[]> mockPriorities = List.of(
                new Object[][]{{TaskPriority.URGENT, 2L}}
        );
        when(taskRepository.countByPriority(orgId)).thenReturn(mockPriorities);

        when(taskRepository.countOverdueByOrganizationId(eq(orgId), any(OffsetDateTime.class))).thenReturn(3L);

        TaskAnalyticsResponse response = analyticsService.getTaskAnalytics(userEmail);

        assertNotNull(response);
        assertEquals(6L, response.tasksByStatus().get(TaskStatus.TODO));
        assertEquals(0L, response.tasksByStatus().get(TaskStatus.DONE));

        assertEquals(2L, response.tasksByPriority().get(TaskPriority.URGENT));
        assertEquals(0L, response.tasksByPriority().get(TaskPriority.LOW));

        assertEquals(3, response.overdueTasks());
    }

    @Test
    void getTaskAnalytics_EmptyDataset() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(taskRepository.countByStatus(orgId)).thenReturn(Collections.emptyList());
        when(taskRepository.countByPriority(orgId)).thenReturn(Collections.emptyList());
        when(taskRepository.countOverdueByOrganizationId(eq(orgId), any(OffsetDateTime.class))).thenReturn(0L);

        TaskAnalyticsResponse response = analyticsService.getTaskAnalytics(userEmail);

        assertNotNull(response);
        assertNotNull(response.tasksByStatus());
        assertEquals(TaskStatus.values().length, response.tasksByStatus().size());
        assertTrue(response.tasksByStatus().values().stream().allMatch(val -> val == 0L));

        assertNotNull(response.tasksByPriority());
        assertEquals(TaskPriority.values().length, response.tasksByPriority().size());
        assertTrue(response.tasksByPriority().values().stream().allMatch(val -> val == 0L));

        assertEquals(0, response.overdueTasks());
    }

    @Test
    void getPublishingAnalytics_Success() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        when(contentRepository.countByStageAndPublishDateRange(eq(orgId), eq(ContentStage.PUBLISHED), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(5L).thenReturn(12L);
        when(contentRepository.countByPublishDateRange(eq(orgId), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(3L).thenReturn(7L);
        when(contentRepository.countByStageAndPublishDateRange(eq(orgId), eq(ContentStage.SCHEDULED), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(2L).thenReturn(4L);

        PublishingAnalyticsResponse response = analyticsService.getPublishingAnalytics(userEmail);

        assertNotNull(response);
        assertEquals(5, response.publishedThisWeek());
        assertEquals(12, response.publishedThisMonth());
        assertEquals(3, response.upcomingThisWeek());
        assertEquals(7, response.upcomingThisMonth());
        assertEquals(2, response.scheduledThisWeek());
        assertEquals(4, response.scheduledThisMonth());
    }

    @Test
    void getDashboardSummary_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> analyticsService.getDashboardSummary(userEmail));
    }
}
