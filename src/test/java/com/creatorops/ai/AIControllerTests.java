package com.creatorops.ai;

import com.creatorops.ai.controller.AIController;
import com.creatorops.ai.service.AIService;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.organization.entity.Organization;
import com.creatorops.research.dto.ResearchItemResponse;
import com.creatorops.research.entity.ResearchItemType;
import com.creatorops.script.dto.ScriptResponse;
import com.creatorops.script.entity.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AIControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIService aiService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User adminUser;
    private User managerUser;
    private User contributorUser;
    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = new Organization("Slay Media", null);
        organization.setId(1L);

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
    }

    private void mockAuth(User user) {
        when(jwtService.extractUsername(anyString())).thenReturn(user.getEmail());
        when(jwtService.isTokenValid(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void generateBrainstorm_AdminSuccess() throws Exception {
        mockAuth(adminUser);
        ResearchItemResponse response = new ResearchItemResponse(
                20L, 10L, 1L, ResearchItemType.AI_BRAINSTORM, "AI Brainstorm - Test", "AI generated brainstorm context", null, OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(aiService.generateBrainstorm(eq(10L), eq("tony@slay.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/contents/10/brainstorm")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.type").value("AI_BRAINSTORM"))
                .andExpect(jsonPath("$.content").value("AI generated brainstorm context"));
    }

    @Test
    void generateScript_ContributorSuccess() throws Exception {
        mockAuth(contributorUser);
        ScriptResponse response = new ScriptResponse(
                30L,
                10L,
                1,
                "Generated script content",
                "Generated script content",
                DocumentType.INTERNAL,
                null,
                null,
                3L,
                "Bruce Contributor",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(aiService.generateScript(eq(10L), eq("bruce@slay.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/contents/10/generate-script")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.editorContent").value("Generated script content"));
    }

    @Test
    void unauthenticated_ReturnsUnauthorizedOrForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/ai/contents/10/brainstorm")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
