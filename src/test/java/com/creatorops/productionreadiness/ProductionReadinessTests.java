package com.creatorops.productionreadiness;

import com.creatorops.ai.service.AIService;
import com.creatorops.auth.dto.LoginRequest;
import com.creatorops.auth.security.JwtService;
import com.creatorops.config.AiRateLimitingInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductionReadinessTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AiRateLimitingInterceptor rateLimitingInterceptor;

    @MockBean
    private AIService aiService;

    private String userToken;

    @BeforeEach
    void setUp() {
        userToken = jwtService.generateToken("admin@creatorops.com", 1L, "Admin User", "ADMIN", 1L);
        rateLimitingInterceptor.clearCache();
    }

    @Test
    void testCorrelationIdFilter_GeneratesUuidIfMissing() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void testCorrelationIdFilter_PropagatesProvidedHeader() throws Exception {
        String clientCorrelationId = "test-correlation-id-12345";
        mockMvc.perform(get("/actuator/health")
                        .header("X-Correlation-Id", clientCorrelationId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", clientCorrelationId));
    }

    @Test
    void testActuatorEndpoints_AreAccessibleAndSafe() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("CreatorOps Backend"));

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    void testApiVersioning_V1PathsSucceedOldPathsReturn404() throws Exception {
        LoginRequest badRequest = new LoginRequest("nonexistent@user.com", "wrongpassword");
        
        // Match the controller endpoint pattern. Bad login credentials on versioned path returns 401 Unauthorized
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isUnauthorized());

        // Test legacy path (returns 404 because RequestMapping was updated to /api/v1/auth)
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAiRateLimiting_ExceededReturns429() throws Exception {
        when(aiService.generateBrainstorm(anyLong(), anyString())).thenReturn(null);

        // Make 5 requests to brainstorm (which is the capacity limit)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/ai/contents/1/brainstorm")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());
        }

        // The 6th request should fail with 429 Too Many Requests
        mockMvc.perform(post("/api/v1/ai/contents/1/brainstorm")
                        .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }
}
