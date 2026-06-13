package com.creatorops.research;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.brand.entity.Brand;
import com.creatorops.content.entity.Content;
import com.creatorops.organization.entity.Organization;
import com.creatorops.research.dto.ResearchItemRequest;
import com.creatorops.research.dto.ResearchItemResponse;
import com.creatorops.research.entity.ResearchItemType;
import com.creatorops.research.service.ResearchItemService;
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
class ResearchItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResearchItemService researchItemService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User contributorUser;
    private Organization organization;
    private Brand brand;
    private Content content;
    private ResearchItemRequest noteRequest;
    private ResearchItemRequest linkRequest;
    private ResearchItemResponse noteResponse;
    private ResearchItemResponse linkResponse;

    @BeforeEach
    void setUp() {
        organization = new Organization("Slay Media", null);
        organization.setId(1L);

        brand = new Brand("SLAY Tech", "Tech review brand", null, organization);
        brand.setId(5L);

        content = new Content(brand, "Unboxing the New AI Chipset", null, null, null, null, null, null);
        content.setId(42L);

        contributorUser = new User("Bruce Contributor", "bruce@slay.com", "hashed_password");
        contributorUser.setId(3L);
        contributorUser.setRole(UserRole.CONTRIBUTOR);
        contributorUser.setOrganization(organization);

        noteRequest = new ResearchItemRequest(
            ResearchItemType.NOTE,
            "Observations from competitor reviews",
            "They focused heavily on the thermals of the chipset.",
            null
        );

        linkRequest = new ResearchItemRequest(
            ResearchItemType.LINK,
            "Competitor unboxing video link",
            null,
            "https://youtube.com/watch?v=competitor-unboxing"
        );

        noteResponse = new ResearchItemResponse(
            100L,
            42L,
            3L,
            ResearchItemType.NOTE,
            "Observations from competitor reviews",
            "They focused heavily on the thermals of the chipset.",
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );

        linkResponse = new ResearchItemResponse(
            101L,
            42L,
            3L,
            ResearchItemType.LINK,
            "Competitor unboxing video link",
            null,
            "https://youtube.com/watch?v=competitor-unboxing",
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
    void createResearchItem_Note_Success() throws Exception {
        mockAuth(contributorUser);
        when(researchItemService.createResearchItem(eq(42L), eq("bruce@slay.com"), any(ResearchItemRequest.class)))
                .thenReturn(noteResponse);

        mockMvc.perform(post("/api/contents/42/research")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.type").value("NOTE"))
                .andExpect(jsonPath("$.title").value("Observations from competitor reviews"))
                .andExpect(jsonPath("$.content").value("They focused heavily on the thermals of the chipset."));
    }

    @Test
    void createResearchItem_Link_Success() throws Exception {
        mockAuth(contributorUser);
        when(researchItemService.createResearchItem(eq(42L), eq("bruce@slay.com"), any(ResearchItemRequest.class)))
                .thenReturn(linkResponse);

        mockMvc.perform(post("/api/contents/42/research")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.type").value("LINK"))
                .andExpect(jsonPath("$.externalUrl").value("https://youtube.com/watch?v=competitor-unboxing"));
    }

    @Test
    void createResearchItem_ValidationFailed_MissingTitle() throws Exception {
        mockAuth(contributorUser);
        ResearchItemRequest invalidRequest = new ResearchItemRequest(
            ResearchItemType.NOTE,
            "", // Blank title
            "Content details",
            null
        );

        mockMvc.perform(post("/api/contents/42/research")
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
    void createResearchItem_ValidationFailed_LinkMissingUrl() throws Exception {
        mockAuth(contributorUser);
        ResearchItemRequest invalidRequest = new ResearchItemRequest(
            ResearchItemType.LINK,
            "Reference Link",
            null,
            "" // Missing URL
        );

        mockMvc.perform(post("/api/contents/42/research")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("externalUrl"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("External URL is required for type LINK"));
    }

    @Test
    void createResearchItem_ValidationFailed_NoteMissingContent() throws Exception {
        mockAuth(contributorUser);
        ResearchItemRequest invalidRequest = new ResearchItemRequest(
            ResearchItemType.NOTE,
            "Reference Note",
            "", // Missing content
            null
        );

        mockMvc.perform(post("/api/contents/42/research")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("content"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("Content is required for type NOTE"));
    }

    @Test
    void getResearchItemById_Success() throws Exception {
        mockAuth(contributorUser);
        when(researchItemService.getResearchItemById(eq(100L), eq("bruce@slay.com")))
                .thenReturn(noteResponse);

        mockMvc.perform(get("/api/research/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("Observations from competitor reviews"));
    }

    @Test
    void getResearchItemById_CrossTenantForbidden() throws Exception {
        mockAuth(contributorUser);
        when(researchItemService.getResearchItemById(eq(100L), eq("bruce@slay.com")))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Access denied: Cannot view research outside your organization."));

        mockMvc.perform(get("/api/research/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void getResearchItemsByContent_Success() throws Exception {
        mockAuth(contributorUser);
        when(researchItemService.getResearchItemsByContent(eq(42L), eq(ResearchItemType.NOTE), eq("bruce@slay.com"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(noteResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/contents/42/research?type=NOTE")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    void updateResearchItem_Success() throws Exception {
        mockAuth(contributorUser);
        when(researchItemService.updateResearchItem(eq(100L), any(ResearchItemRequest.class), eq("bruce@slay.com")))
                .thenReturn(noteResponse);

        mockMvc.perform(put("/api/research/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void deleteResearchItem_Success() throws Exception {
        mockAuth(contributorUser);
        doNothing().when(researchItemService).deleteResearchItem(eq(100L), eq("bruce@slay.com"));

        mockMvc.perform(delete("/api/research/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
