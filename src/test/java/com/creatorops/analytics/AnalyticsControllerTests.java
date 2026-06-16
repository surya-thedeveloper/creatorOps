package com.creatorops.analytics;

import com.creatorops.analytics.controller.AnalyticsController;
import com.creatorops.analytics.dto.*;
import com.creatorops.analytics.service.AnalyticsService;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.organization.entity.Organization;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.entity.ContentPriority;
import com.creatorops.assignment.entity.AssignmentStatus;
import com.creatorops.assignment.entity.AssignmentType;
import com.creatorops.task.entity.TaskStatus;
import com.creatorops.task.entity.TaskPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User adminUser;
    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = new Organization("Slay Media", null);
        organization.setId(1L);

        adminUser = new User("Tony Admin", "tony@slay.com", "hashed_password");
        adminUser.setId(1L);
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(organization);
    }

    private void mockAuth(User user) {
        when(jwtService.extractUsername(anyString())).thenReturn(user.getEmail());
        when(jwtService.isTokenValid(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void getDashboardSummary_Success() throws Exception {
        mockAuth(adminUser);
        DashboardSummaryResponse mockResponse = new DashboardSummaryResponse(
                10, 3, 4, 2, 8, 5, 15, 10, 1, 12
        );
        when(analyticsService.getDashboardSummary("tony@slay.com")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalContent").value(10))
                .andExpect(jsonPath("$.scheduledContent").value(3))
                .andExpect(jsonPath("$.publishedContent").value(4))
                .andExpect(jsonPath("$.overdueContent").value(2))
                .andExpect(jsonPath("$.totalAssignments").value(8))
                .andExpect(jsonPath("$.activeAssignments").value(5))
                .andExpect(jsonPath("$.totalTasks").value(15))
                .andExpect(jsonPath("$.completedTasks").value(10))
                .andExpect(jsonPath("$.overdueTasks").value(1))
                .andExpect(jsonPath("$.totalAssets").value(12));
    }

    @Test
    void getContentAnalytics_Success() throws Exception {
        mockAuth(adminUser);
        Map<ContentStage, Long> stageMap = new EnumMap<>(ContentStage.class);
        stageMap.put(ContentStage.IDEA, 5L);
        Map<ContentType, Long> typeMap = new EnumMap<>(ContentType.class);
        typeMap.put(ContentType.YOUTUBE_VIDEO, 3L);
        Map<ContentPriority, Long> priorityMap = new EnumMap<>(ContentPriority.class);
        priorityMap.put(ContentPriority.HIGH, 4L);

        ContentAnalyticsResponse mockResponse = new ContentAnalyticsResponse(stageMap, typeMap, priorityMap);
        when(analyticsService.getContentAnalytics("tony@slay.com")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/content")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentByStage.IDEA").value(5))
                .andExpect(jsonPath("$.contentByType.YOUTUBE_VIDEO").value(3))
                .andExpect(jsonPath("$.contentByPriority.HIGH").value(4));
    }

    @Test
    void getAssignmentAnalytics_Success() throws Exception {
        mockAuth(adminUser);
        Map<AssignmentStatus, Long> statusMap = new EnumMap<>(AssignmentStatus.class);
        statusMap.put(AssignmentStatus.IN_PROGRESS, 3L);
        Map<AssignmentType, Long> typeMap = new EnumMap<>(AssignmentType.class);
        typeMap.put(AssignmentType.SCRIPT, 4L);

        AssignmentAnalyticsResponse mockResponse = new AssignmentAnalyticsResponse(statusMap, typeMap);
        when(analyticsService.getAssignmentAnalytics("tony@slay.com")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/assignments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentsByStatus.IN_PROGRESS").value(3))
                .andExpect(jsonPath("$.assignmentsByType.SCRIPT").value(4));
    }

    @Test
    void getTaskAnalytics_Success() throws Exception {
        mockAuth(adminUser);
        Map<TaskStatus, Long> statusMap = new EnumMap<>(TaskStatus.class);
        statusMap.put(TaskStatus.TODO, 6L);
        Map<TaskPriority, Long> priorityMap = new EnumMap<>(TaskPriority.class);
        priorityMap.put(TaskPriority.URGENT, 2L);

        TaskAnalyticsResponse mockResponse = new TaskAnalyticsResponse(statusMap, priorityMap, 3);
        when(analyticsService.getTaskAnalytics("tony@slay.com")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasksByStatus.TODO").value(6))
                .andExpect(jsonPath("$.tasksByPriority.URGENT").value(2))
                .andExpect(jsonPath("$.overdueTasks").value(3));
    }

    @Test
    void getPublishingAnalytics_Success() throws Exception {
        mockAuth(adminUser);
        PublishingAnalyticsResponse mockResponse = new PublishingAnalyticsResponse(
                5, 12, 3, 7, 2, 4
        );
        when(analyticsService.getPublishingAnalytics("tony@slay.com")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/publishing")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedThisWeek").value(5))
                .andExpect(jsonPath("$.publishedThisMonth").value(12))
                .andExpect(jsonPath("$.upcomingThisWeek").value(3))
                .andExpect(jsonPath("$.upcomingThisMonth").value(7))
                .andExpect(jsonPath("$.scheduledThisWeek").value(2))
                .andExpect(jsonPath("$.scheduledThisMonth").value(4));
    }

    @Test
    void unauthenticated_ReturnsUnauthorizedOrForbidden() throws Exception {
        // Accessing endpoint without header should be rejected by Spring Security Filter Chain
        mockMvc.perform(get("/api/analytics/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // Or status().isForbidden() depending on EntryPoint config
    }
}
