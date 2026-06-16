package com.creatorops.assignment;

import com.creatorops.assignment.dto.AssignmentRequest;
import com.creatorops.assignment.dto.AssignmentResponse;
import com.creatorops.assignment.dto.AssignmentStatusRequest;
import com.creatorops.assignment.entity.AssignmentStatus;
import com.creatorops.assignment.entity.AssignmentType;
import com.creatorops.assignment.service.AssignmentService;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.brand.entity.Brand;
import com.creatorops.content.entity.Content;
import com.creatorops.organization.entity.Organization;
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
class AssignmentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssignmentService assignmentService;

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
    private AssignmentRequest validRequest;
    private AssignmentResponse validResponse;

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

        validRequest = new AssignmentRequest(
            3L,
            AssignmentType.SCRIPT,
            "Write the script using script guidelines.",
            OffsetDateTime.now().plusDays(5)
        );

        validResponse = new AssignmentResponse(
            100L,
            42L,
            3L,
            "Bruce Contributor",
            2L,
            "Tony Manager",
            AssignmentType.SCRIPT,
            AssignmentStatus.ASSIGNED,
            "Write the script using script guidelines.",
            OffsetDateTime.now().plusDays(5),
            null,
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
    void createAssignment_Success() throws Exception {
        mockAuth(managerUser);
        when(assignmentService.createAssignment(eq(42L), eq("tony@slay.com"), any(AssignmentRequest.class)))
                .thenReturn(validResponse);

        mockMvc.perform(post("/api/contents/42/assignments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.assignmentType").value("SCRIPT"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedToUserName").value("Bruce Contributor"));
    }

    @Test
    void createAssignment_ValidationFailed_MissingType() throws Exception {
        mockAuth(managerUser);
        AssignmentRequest invalidRequest = new AssignmentRequest(
            3L,
            null, // Missing type
            "Write the script",
            OffsetDateTime.now().plusDays(5)
        );

        mockMvc.perform(post("/api/contents/42/assignments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("assignmentType"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("Assignment type is required"));
    }

    @Test
    void createAssignment_PastDueDate_ReturnsBadRequest() throws Exception {
        mockAuth(managerUser);
        AssignmentRequest pastRequest = new AssignmentRequest(
            3L,
            AssignmentType.SCRIPT,
            "Write the script",
            OffsetDateTime.now().minusDays(1) // Past due date
        );

        when(assignmentService.createAssignment(eq(42L), eq("tony@slay.com"), any(AssignmentRequest.class)))
                .thenThrow(new IllegalArgumentException("Validation failed: Due date cannot be in the past."));

        mockMvc.perform(post("/api/contents/42/assignments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pastRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed: Due date cannot be in the past."));
    }

    @Test
    void getAssignmentById_Success() throws Exception {
        mockAuth(contributorUser);
        when(assignmentService.getAssignmentById(eq(100L), eq("bruce@slay.com"))).thenReturn(validResponse);

        mockMvc.perform(get("/api/assignments/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.assignmentType").value("SCRIPT"));
    }

    @Test
    void getAssignmentById_CrossTenantForbidden() throws Exception {
        mockAuth(otherTenantUser);
        when(assignmentService.getAssignmentById(eq(100L), eq("thor@other.com")))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Access denied: Assignment belongs to a different organization."));

        mockMvc.perform(get("/api/assignments/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void updateAssignmentStatus_Contributor_OwnAssignment_Success() throws Exception {
        mockAuth(contributorUser);
        AssignmentStatusRequest statusRequest = new AssignmentStatusRequest(AssignmentStatus.IN_PROGRESS);
        AssignmentResponse updatedResponse = new AssignmentResponse(
            100L, 42L, 3L, "Bruce Contributor", 2L, "Tony Manager",
            AssignmentType.SCRIPT, AssignmentStatus.IN_PROGRESS, "Notes",
            null, OffsetDateTime.now(), null, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(assignmentService.updateAssignmentStatus(eq(100L), eq("bruce@slay.com"), any(AssignmentStatusRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/assignments/100/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateAssignmentStatus_Contributor_NotOwnAssignment_Forbidden() throws Exception {
        mockAuth(contributorUser);
        AssignmentStatusRequest statusRequest = new AssignmentStatusRequest(AssignmentStatus.IN_PROGRESS);

        when(assignmentService.updateAssignmentStatus(eq(100L), eq("bruce@slay.com"), any(AssignmentStatusRequest.class)))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Access denied: Contributors can only update the status of assignments assigned to themselves."));

        mockMvc.perform(patch("/api/assignments/100/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getMyAssignments_FilterByStatus() throws Exception {
        mockAuth(contributorUser);
        when(assignmentService.getMyAssignments(eq("bruce@slay.com"), eq(AssignmentStatus.ASSIGNED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/assignments/my?status=ASSIGNED")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    void deleteAssignment_Success() throws Exception {
        mockAuth(managerUser);
        doNothing().when(assignmentService).deleteAssignment(eq(100L), eq("tony@slay.com"));

        mockMvc.perform(delete("/api/assignments/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAssignment_Contributor_Forbidden() throws Exception {
        mockAuth(contributorUser);
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied: Only ADMIN or MANAGER can manage assignments."))
                .when(assignmentService).deleteAssignment(eq(100L), eq("bruce@slay.com"));

        mockMvc.perform(delete("/api/assignments/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
