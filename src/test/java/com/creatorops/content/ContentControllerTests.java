package com.creatorops.content;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.brand.entity.Brand;
import com.creatorops.content.controller.ContentController;
import com.creatorops.content.dto.ContentRequest;
import com.creatorops.content.dto.ContentResponse;
import com.creatorops.content.entity.ContentPriority;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.service.ContentService;
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
class ContentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContentService contentService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User adminUser;
    private User managerUser;
    private User contributorUser;
    private Organization organization;
    private Brand brand;
    private ContentRequest contentRequest;
    private ContentResponse contentResponse;

    @BeforeEach
    void setUp() {
        organization = new Organization("Slay Media", null);
        organization.setId(1L);

        brand = new Brand("SLAY Tech", "Tech review brand", null, organization);
        brand.setId(5L);

        adminUser = new User("Tony Admin", "tony@slay.com", "hashed_password");
        adminUser.setId(1L);
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(organization);

        managerUser = new User("Rogers Manager", "rogers@slay.com", "hashed_password");
        managerUser.setId(2L);
        managerUser.setRole(UserRole.MANAGER);
        managerUser.setOrganization(organization);

        contributorUser = new User("Bruce Contributor", "bruce@slay.com", "hashed_password");
        contributorUser.setId(3L);
        contributorUser.setRole(UserRole.CONTRIBUTOR);
        contributorUser.setOrganization(organization);

        contentRequest = new ContentRequest(
            5L,
            "10 Coding Habits that Will Make You a Staff Engineer",
            "This is a detailed outline on habits",
            ContentType.BLOG,
            ContentStage.IDEA,
            ContentPriority.HIGH,
            OffsetDateTime.parse("2026-07-01T12:00:00Z"),
            OffsetDateTime.parse("2026-07-05T12:00:00Z")
        );

        contentResponse = new ContentResponse(
            42L,
            5L,
            "10 Coding Habits that Will Make You a Staff Engineer",
            "This is a detailed outline on habits",
            ContentType.BLOG,
            ContentStage.IDEA,
            ContentPriority.HIGH,
            OffsetDateTime.parse("2026-07-01T12:00:00Z"),
            OffsetDateTime.parse("2026-07-05T12:00:00Z"),
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
    void createContent_AdminSuccess() throws Exception {
        mockAuth(adminUser);
        when(contentService.createContent(eq("tony@slay.com"), any(ContentRequest.class)))
                .thenReturn(contentResponse);

        mockMvc.perform(post("/api/contents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.title").value("10 Coding Habits that Will Make You a Staff Engineer"))
                .andExpect(jsonPath("$.brandId").value(5))
                .andExpect(jsonPath("$.type").value("BLOG"))
                .andExpect(jsonPath("$.stage").value("IDEA"));
    }

    @Test
    void createContent_ManagerSuccess() throws Exception {
        mockAuth(managerUser);
        when(contentService.createContent(eq("rogers@slay.com"), any(ContentRequest.class)))
                .thenReturn(contentResponse);

        mockMvc.perform(post("/api/contents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void createContent_ContributorForbidden() throws Exception {
        mockAuth(contributorUser);

        mockMvc.perform(post("/api/contents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contentRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createContent_ValidationFailed_MissingTitle() throws Exception {
        mockAuth(adminUser);
        ContentRequest invalidRequest = new ContentRequest(
            5L,
            "", // Blank title
            "Description",
            ContentType.BLOG,
            ContentStage.IDEA,
            ContentPriority.MEDIUM,
            null,
            null
        );

        mockMvc.perform(post("/api/contents")
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
    void createContent_ValidationFailed_NullType() throws Exception {
        mockAuth(adminUser);
        // We bypass compile-time enum nullability by writing raw JSON string
        String invalidJson = """
            {
                "brandId": 5,
                "title": "Title",
                "type": null,
                "stage": "IDEA",
                "priority": "MEDIUM"
            }
            """;

        mockMvc.perform(post("/api/contents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("type"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("Content type is required"));
    }

    @Test
    void getContentById_Success() throws Exception {
        mockAuth(contributorUser);
        when(contentService.getContentById(eq(42L), eq("bruce@slay.com")))
                .thenReturn(contentResponse);

        mockMvc.perform(get("/api/contents/42")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.title").value("10 Coding Habits that Will Make You a Staff Engineer"));
    }

    @Test
    void getContents_Success() throws Exception {
        mockAuth(contributorUser);
        when(contentService.searchContents(eq("bruce@slay.com"), eq(5L), eq(ContentStage.IDEA), eq(ContentType.BLOG), eq("Staff"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(contentResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/contents?brandId=5&stage=IDEA&type=BLOG&title=Staff")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(42))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    void updateContent_AdminSuccess() throws Exception {
        mockAuth(adminUser);
        when(contentService.updateContent(eq(42L), any(ContentRequest.class), eq("tony@slay.com")))
                .thenReturn(contentResponse);

        mockMvc.perform(put("/api/contents/42")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void deleteContent_AdminSuccess() throws Exception {
        mockAuth(adminUser);
        doNothing().when(contentService).deleteContent(eq(42L), eq("tony@slay.com"));

        mockMvc.perform(delete("/api/contents/42")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
