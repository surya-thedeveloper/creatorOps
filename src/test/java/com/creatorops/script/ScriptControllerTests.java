package com.creatorops.script;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.brand.entity.Brand;
import com.creatorops.content.entity.Content;
import com.creatorops.organization.entity.Organization;
import com.creatorops.script.dto.ScriptRequest;
import com.creatorops.script.dto.ScriptResponse;
import com.creatorops.script.entity.DocumentType;
import com.creatorops.script.service.ScriptService;
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
class ScriptControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScriptService scriptService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User creatorUser;
    private Organization organization;
    private Brand brand;
    private Content content;
    private ScriptRequest internalRequest;
    private ScriptRequest googleDocRequest;
    private ScriptResponse internalResponseV1;
    private ScriptResponse googleDocResponseV2;

    @BeforeEach
    void setUp() {
        organization = new Organization("Slay Media", null);
        organization.setId(1L);

        brand = new Brand("SLAY Tech", "Tech review brand", null, organization);
        brand.setId(5L);

        content = new Content(brand, "Unboxing the New AI Chipset", null, null, null, null, null, null);
        content.setId(42L);

        creatorUser = new User("Bruce Contributor", "bruce@slay.com", "hashed_password");
        creatorUser.setId(3L);
        creatorUser.setRole(UserRole.CONTRIBUTOR);
        creatorUser.setOrganization(organization);

        internalRequest = new ScriptRequest(
            DocumentType.INTERNAL,
            "Script internal editor draft text contents.",
            null,
            null,
            "AI Generated baseline draft content."
        );

        googleDocRequest = new ScriptRequest(
            DocumentType.GOOGLE_DOC,
            null,
            "https://docs.google.com/document/d/external-doc-id",
            null,
            null
        );

        internalResponseV1 = new ScriptResponse(
            100L,
            42L,
            1,
            "AI Generated baseline draft content.",
            "Script internal editor draft text contents.",
            DocumentType.INTERNAL,
            null,
            null,
            3L,
            "Bruce Contributor",
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );

        googleDocResponseV2 = new ScriptResponse(
            101L,
            42L,
            2,
            null,
            null,
            DocumentType.GOOGLE_DOC,
            "https://docs.google.com/document/d/external-doc-id",
            null,
            3L,
            "Bruce Contributor",
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
    void createScript_Internal_Success() throws Exception {
        mockAuth(creatorUser);
        when(scriptService.createScript(eq(42L), eq("bruce@slay.com"), any(ScriptRequest.class)))
                .thenReturn(internalResponseV1);

        mockMvc.perform(post("/api/contents/42/scripts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(internalRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.documentType").value("INTERNAL"))
                .andExpect(jsonPath("$.editorContent").value("Script internal editor draft text contents."));
    }

    @Test
    void createScript_GoogleDoc_Success() throws Exception {
        mockAuth(creatorUser);
        when(scriptService.createScript(eq(42L), eq("bruce@slay.com"), any(ScriptRequest.class)))
                .thenReturn(googleDocResponseV2);

        mockMvc.perform(post("/api/contents/42/scripts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleDocRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.documentType").value("GOOGLE_DOC"))
                .andExpect(jsonPath("$.externalDocumentUrl").value("https://docs.google.com/document/d/external-doc-id"));
    }

    @Test
    void createScript_ValidationFailed_InternalMissingEditorContent() throws Exception {
        mockAuth(creatorUser);
        ScriptRequest invalidRequest = new ScriptRequest(
            DocumentType.INTERNAL,
            "", // Blank editor content
            null,
            null,
            null
        );

        mockMvc.perform(post("/api/contents/42/scripts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("editorContent"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("Editor content is required for document type INTERNAL"));
    }

    @Test
    void createScript_ValidationFailed_GoogleDocMissingUrl() throws Exception {
        mockAuth(creatorUser);
        ScriptRequest invalidRequest = new ScriptRequest(
            DocumentType.GOOGLE_DOC,
            null,
            "", // Blank URL
            null,
            null
        );

        mockMvc.perform(post("/api/contents/42/scripts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("externalDocumentUrl"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("External document URL is required for document type GOOGLE_DOC"));
    }

    @Test
    void getScriptById_Success() throws Exception {
        mockAuth(creatorUser);
        when(scriptService.getScriptById(eq(100L), eq("bruce@slay.com"))).thenReturn(internalResponseV1);

        mockMvc.perform(get("/api/scripts/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void getScriptById_CrossTenantForbidden() throws Exception {
        mockAuth(creatorUser);
        when(scriptService.getScriptById(eq(100L), eq("bruce@slay.com")))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Access denied: Script belongs to a different organization."));

        mockMvc.perform(get("/api/scripts/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void getScriptsByContent_Success() throws Exception {
        mockAuth(creatorUser);
        when(scriptService.getScriptsByContent(eq(42L), eq("bruce@slay.com"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(googleDocResponseV2, internalResponseV1), PageRequest.of(0, 20), 2));

        mockMvc.perform(get("/api/contents/42/scripts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].version").value(2))
                .andExpect(jsonPath("$.content[1].version").value(1))
                .andExpect(jsonPath("$.pagination.totalElements").value(2));
    }

    @Test
    void updateScript_Success() throws Exception {
        mockAuth(creatorUser);
        when(scriptService.updateScript(eq(100L), any(ScriptRequest.class), eq("bruce@slay.com")))
                .thenReturn(internalResponseV1);

        mockMvc.perform(put("/api/scripts/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(internalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void deleteScript_Success() throws Exception {
        mockAuth(creatorUser);
        doNothing().when(scriptService).deleteScript(eq(100L), eq("bruce@slay.com"));

        mockMvc.perform(delete("/api/scripts/100")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
