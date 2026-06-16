package com.creatorops.organization;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.organization.dto.OrganizationRequest;
import com.creatorops.organization.dto.OrganizationResponse;
import com.creatorops.organization.entity.Organization;
import com.creatorops.organization.service.OrganizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import java.time.OffsetDateTime;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrganizationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationService organizationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User adminUser;
    private User managerUser;
    private Organization organization1;
    private Organization organization2;
    private OrganizationResponse orgResponse1;

    @BeforeEach
    void setUp() {
        organization1 = new Organization("SLAY Media", "https://example.com/logo1.png");
        organization1.setId(1L);

        organization2 = new Organization("Sony Media", "https://example.com/logo2.png");
        organization2.setId(2L);

        orgResponse1 = new OrganizationResponse(
                1L, "SLAY Media", "https://example.com/logo1.png", OffsetDateTime.now(), OffsetDateTime.now());

        adminUser = new User("Tony Admin", "tony@slay.media", "password_hash");
        adminUser.setId(10L);
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(organization1);

        managerUser = new User("Rogers Manager", "rogers@slay.media", "password_hash");
        managerUser.setId(11L);
        managerUser.setRole(UserRole.MANAGER);
        managerUser.setOrganization(organization1);

        // Standard token mocks
        when(jwtService.isTokenValid(anyString(), anyString())).thenReturn(true);
    }

    @Test
    void createOrganization_Admin_ReturnsCreated() throws Exception {
        OrganizationRequest request = new OrganizationRequest("SLAY Media", "https://example.com/logo1.png");
        when(jwtService.extractUsername(anyString())).thenReturn("tony@slay.media");
        when(userRepository.findByEmail("tony@slay.media")).thenReturn(Optional.of(adminUser));
        when(organizationService.createOrganization(any(OrganizationRequest.class))).thenReturn(orgResponse1);

        mockMvc.perform(post("/api/v1/organizations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_admin_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("SLAY Media"));

        verify(organizationService, times(1)).createOrganization(any(OrganizationRequest.class));
    }

    @Test
    void createOrganization_Manager_ReturnsForbidden() throws Exception {
        OrganizationRequest request = new OrganizationRequest("SLAY Media", "https://example.com/logo1.png");
        when(jwtService.extractUsername(anyString())).thenReturn("rogers@slay.media");
        when(userRepository.findByEmail("rogers@slay.media")).thenReturn(Optional.of(managerUser));

        mockMvc.perform(post("/api/v1/organizations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_manager_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createOrganization_Unauthenticated_ReturnsUnauthorized() throws Exception {
        OrganizationRequest request = new OrganizationRequest("SLAY Media", "https://example.com/logo1.png");

        mockMvc.perform(post("/api/v1/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateOrganization_OwnOrganization_ReturnsOk() throws Exception {
        OrganizationRequest request = new OrganizationRequest("Updated Name", "https://example.com/logo1.png");
        OrganizationResponse updatedResponse = new OrganizationResponse(
                1L, "Updated Name", "https://example.com/logo1.png", OffsetDateTime.now(), OffsetDateTime.now());

        when(jwtService.extractUsername(anyString())).thenReturn("tony@slay.media");
        when(userRepository.findByEmail("tony@slay.media")).thenReturn(Optional.of(adminUser));
        when(organizationService.updateOrganization(eq(1L), any(OrganizationRequest.class), eq("tony@slay.media")))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/organizations/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_admin_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateOrganization_OtherOrganization_ReturnsForbidden() throws Exception {
        OrganizationRequest request = new OrganizationRequest("Updated Name", "https://example.com/logo2.png");
        when(jwtService.extractUsername(anyString())).thenReturn("tony@slay.media");
        when(userRepository.findByEmail("tony@slay.media")).thenReturn(Optional.of(adminUser));
        
        // Mock service throwing AccessDeniedException for tenant crossing
        when(organizationService.updateOrganization(eq(2L), any(OrganizationRequest.class), eq("tony@slay.media")))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(put("/api/v1/organizations/2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_admin_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void deleteOrganization_OwnOrganization_ReturnsNoContent() throws Exception {
        when(jwtService.extractUsername(anyString())).thenReturn("tony@slay.media");
        when(userRepository.findByEmail("tony@slay.media")).thenReturn(Optional.of(adminUser));

        mockMvc.perform(delete("/api/v1/organizations/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_admin_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(organizationService, times(1)).deleteOrganization(eq(1L), eq("tony@slay.media"));
    }

    @Test
    void deleteOrganization_OtherOrganization_ReturnsForbidden() throws Exception {
        when(jwtService.extractUsername(anyString())).thenReturn("tony@slay.media");
        when(userRepository.findByEmail("tony@slay.media")).thenReturn(Optional.of(adminUser));
        
        doThrow(new AccessDeniedException("Access denied"))
                .when(organizationService).deleteOrganization(eq(2L), eq("tony@slay.media"));

        mockMvc.perform(delete("/api/v1/organizations/2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_admin_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
