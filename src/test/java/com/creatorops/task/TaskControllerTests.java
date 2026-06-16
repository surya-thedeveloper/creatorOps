package com.creatorops.task;

import com.creatorops.assignment.entity.Assignment;
import com.creatorops.assignment.entity.AssignmentType;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.brand.entity.Brand;
import com.creatorops.content.entity.Content;
import com.creatorops.organization.entity.Organization;
import com.creatorops.task.dto.TaskRequest;
import com.creatorops.task.dto.TaskResponse;
import com.creatorops.task.dto.TaskStatusRequest;
import com.creatorops.task.entity.TaskPriority;
import com.creatorops.task.entity.TaskStatus;
import com.creatorops.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User managerUser;
    private User contributorUser;
    private User otherTenantUser;
    private Organization organization;
    private Organization otherOrganization;
    private Brand brand;
    private Content content;
    private Assignment assignment;
    private TaskRequest validRequest;
    private TaskResponse validResponse;

    @BeforeEach
    void setUp() {
        organization = new Organization("Slay Media", null);
        organization.setId(1L);

        otherOrganization = new Organization("Other Inc", null);
        otherOrganization.setId(2L);

        brand = new Brand("SLAY Tech", "Tech review brand", null, organization);
        brand.setId(5L);

        content = new Content(brand, "Summer Fashion Guide", null, null, null, null, null, null);
        content.setId(42L);

        assignment = new Assignment();
        assignment.setId(100L);
        assignment.setContent(content);
        assignment.setAssignmentType(AssignmentType.SCRIPT);

        managerUser = new User("Tony Manager", "tony@slay.com", "hashed_password");
        managerUser.setId(2L);
        managerUser.setRole(UserRole.MANAGER);
        managerUser.setOrganization(organization);

        contributorUser = new User("Bruce Contributor", "bruce@slay.com", "hashed_password");
        contributorUser.setId(3L);
        contributorUser.setRole(UserRole.CONTRIBUTOR);
        contributorUser.setOrganization(organization);

        otherTenantUser = new User("Thor Other", "thor@other.com", "hashed_password");
        otherTenantUser.setId(4L);
        otherTenantUser.setRole(UserRole.CONTRIBUTOR);
        otherTenantUser.setOrganization(otherOrganization);

        validRequest = new TaskRequest(
            "Write Hook",
            "Draft the retention-optimized hook.",
            TaskPriority.HIGH,
            3L,
            OffsetDateTime.now().plusDays(5)
        );

        validResponse = new TaskResponse(
            500L,
            100L,
            "SCRIPT",
            42L,
            "Summer Fashion Guide",
            3L,
            "Bruce Contributor",
            2L,
            "Tony Manager",
            "Write Hook",
            "Draft the retention-optimized hook.",
            TaskStatus.TODO,
            TaskPriority.HIGH,
            OffsetDateTime.now().plusDays(5),
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
    }

    private void mockAuth(User user) {
        when(jwtService.extractUsername(anyString())).thenReturn(user.getEmail());
        when(jwtService.isTokenValid(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void createTask_Success() throws Exception {
        mockAuth(managerUser);
        when(taskService.createTask(eq(100L), eq("tony@slay.com"), any(TaskRequest.class)))
                .thenReturn(validResponse);

        mockMvc.perform(post("/api/v1/assignments/100/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.title").value("Write Hook"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createTask_ValidationFailed_BlankTitle() throws Exception {
        mockAuth(managerUser);
        TaskRequest invalidRequest = new TaskRequest(
            "", // Blank title
            "Description",
            TaskPriority.HIGH,
            3L,
            OffsetDateTime.now().plusDays(5)
        );

        mockMvc.perform(post("/api/v1/assignments/100/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("title"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("Title is required"));
    }

    @Test
    void getTask_Success() throws Exception {
        mockAuth(contributorUser);
        when(taskService.getTaskById(eq(500L), eq("bruce@slay.com"))).thenReturn(validResponse);

        mockMvc.perform(get("/api/v1/tasks/500")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.title").value("Write Hook"));
    }

    @Test
    void getTasksByAssignment_Success() throws Exception {
        mockAuth(contributorUser);
        when(taskService.getTasksByAssignment(eq(100L), eq("bruce@slay.com"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/assignments/100/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(500))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    void getMyTasks_Success() throws Exception {
        mockAuth(contributorUser);
        when(taskService.getMyTasks(eq("bruce@slay.com"), eq(TaskStatus.TODO), eq(TaskPriority.HIGH), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/tasks/my?status=TODO&priority=HIGH")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(500))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    void updateTask_Success() throws Exception {
        mockAuth(managerUser);
        when(taskService.updateTask(eq(500L), eq("tony@slay.com"), any(TaskRequest.class)))
                .thenReturn(validResponse);

        mockMvc.perform(put("/api/v1/tasks/500")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.title").value("Write Hook"));
    }

    @Test
    void updateTaskStatus_Success() throws Exception {
        mockAuth(contributorUser);
        TaskStatusRequest statusRequest = new TaskStatusRequest(TaskStatus.DONE);
        TaskResponse doneResponse = new TaskResponse(
            500L, 100L, "SCRIPT", 42L, "Summer Fashion Guide",
            3L, "Bruce Contributor", 2L, "Tony Manager",
            "Write Hook", "Draft the retention-optimized hook.",
            TaskStatus.DONE, TaskPriority.HIGH, OffsetDateTime.now().plusDays(5),
            OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(taskService.updateTaskStatus(eq(500L), eq("bruce@slay.com"), any(TaskStatusRequest.class)))
                .thenReturn(doneResponse);

        mockMvc.perform(patch("/api/v1/tasks/500/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void deleteTask_Success() throws Exception {
        mockAuth(managerUser);
        doNothing().when(taskService).deleteTask(eq(500L), eq("tony@slay.com"));

        mockMvc.perform(delete("/api/v1/tasks/500")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_Contributor_Forbidden() throws Exception {
        mockAuth(contributorUser);
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied: Only ADMIN or MANAGER can manage tasks."))
                .when(taskService).deleteTask(eq(500L), eq("bruce@slay.com"));

        mockMvc.perform(delete("/api/v1/tasks/500")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
