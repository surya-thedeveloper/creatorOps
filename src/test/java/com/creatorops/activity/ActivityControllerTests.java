package com.creatorops.activity;

import com.creatorops.activity.dto.ActivityResponse;
import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;
import com.creatorops.activity.service.ActivityService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ActivityControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityService activityService;

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
    private ActivityResponse validResponse;

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

        validResponse = new ActivityResponse(
            10L,
            42L,
            3L,
            "Bruce Contributor",
            EventType.CONTENT_CREATED,
            EntityType.CONTENT,
            42L,
            "Content 'Summer Fashion Guide' was created",
            null,
            OffsetDateTime.now()
        );
    }

    private void mockAuth(User user) {
        when(jwtService.extractUsername(anyString())).thenReturn(user.getEmail());
        when(jwtService.isTokenValid(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void getActivitiesByContent_Success() throws Exception {
        mockAuth(contributorUser);
        when(activityService.getActivitiesByContent(eq(42L), eq("bruce@slay.com"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/contents/42/activities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].eventType").value("CONTENT_CREATED"))
                .andExpect(jsonPath("$.content[0].description").value("Content 'Summer Fashion Guide' was created"))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    void getActivitiesByContent_CrossTenantForbidden() throws Exception {
        mockAuth(otherTenantUser);
        when(activityService.getActivitiesByContent(eq(42L), eq("thor@other.com"), any(Pageable.class)))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Access denied: Content belongs to a different organization."));

        mockMvc.perform(get("/api/contents/42/activities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void getActivityById_Success() throws Exception {
        mockAuth(contributorUser);
        when(activityService.getActivityById(eq(10L), eq("bruce@slay.com"))).thenReturn(validResponse);

        mockMvc.perform(get("/api/activities/10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("Content 'Summer Fashion Guide' was created"));
    }

    @Test
    void getActivityById_CrossTenantForbidden() throws Exception {
        mockAuth(otherTenantUser);
        when(activityService.getActivityById(eq(10L), eq("thor@other.com")))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Access denied: Activity belongs to a different organization."));

        mockMvc.perform(get("/api/activities/10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
