package com.creatorops.asset;

import com.creatorops.asset.dto.AssetRequest;
import com.creatorops.asset.dto.AssetResponse;
import com.creatorops.asset.entity.AssetSource;
import com.creatorops.asset.entity.AssetType;
import com.creatorops.asset.service.AssetService;
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
import org.springframework.security.access.AccessDeniedException;
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
class AssetControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetService assetService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User managerUser;
    private User contributorUser;
    private Organization organization;
    private Brand brand;
    private Content content;
    private AssetRequest validRequest;
    private AssetResponse validResponse;

    @BeforeEach
    void setUp() {
        organization = new Organization("Slay Media", null);
        organization.setId(1L);

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

        validRequest = new AssetRequest(
            "Thumbnail V1",
            "Summer Fashion Guide Draft Thumbnail",
            AssetType.THUMBNAIL,
            AssetSource.GOOGLE_DRIVE,
            "https://drive.google.com/thumbnail1.jpg",
            204800L,
            "image/jpeg",
            1
        );

        validResponse = new AssetResponse(
            500L,
            42L,
            "Summer Fashion Guide",
            3L,
            "Bruce Contributor",
            AssetType.THUMBNAIL,
            AssetSource.GOOGLE_DRIVE,
            "Thumbnail V1",
            "Summer Fashion Guide Draft Thumbnail",
            "https://drive.google.com/thumbnail1.jpg",
            204800L,
            "image/jpeg",
            1,
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
    void createAsset_Success() throws Exception {
        mockAuth(contributorUser);
        when(assetService.createAsset(eq(42L), eq("bruce@slay.com"), any(AssetRequest.class)))
                .thenReturn(validResponse);

        mockMvc.perform(post("/api/contents/42/assets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.name").value("Thumbnail V1"))
                .andExpect(jsonPath("$.assetType").value("THUMBNAIL"))
                .andExpect(jsonPath("$.assetSource").value("GOOGLE_DRIVE"));
    }

    @Test
    void createAsset_ValidationFailed_EmptyName() throws Exception {
        mockAuth(contributorUser);
        AssetRequest invalidRequest = new AssetRequest(
            "", // Blank name
            "Summer Fashion Guide Draft Thumbnail",
            AssetType.THUMBNAIL,
            AssetSource.GOOGLE_DRIVE,
            "https://drive.google.com/thumbnail1.jpg",
            204800L,
            "image/jpeg",
            1
        );

        mockMvc.perform(post("/api/contents/42/assets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("Asset name is required"));
    }

    @Test
    void getAsset_Success() throws Exception {
        mockAuth(contributorUser);
        when(assetService.getAssetById(eq(500L), eq("bruce@slay.com"))).thenReturn(validResponse);

        mockMvc.perform(get("/api/assets/500")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.name").value("Thumbnail V1"));
    }

    @Test
    void getAssetsByContent_Success() throws Exception {
        mockAuth(contributorUser);
        when(assetService.getAssetsByContent(eq(42L), eq("bruce@slay.com"), eq(AssetType.THUMBNAIL), eq(AssetSource.GOOGLE_DRIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/contents/42/assets?assetType=THUMBNAIL&assetSource=GOOGLE_DRIVE")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(500))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    void updateAsset_Success() throws Exception {
        mockAuth(contributorUser);
        when(assetService.updateAsset(eq(500L), eq("bruce@slay.com"), any(AssetRequest.class)))
                .thenReturn(validResponse);

        mockMvc.perform(put("/api/assets/500")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.name").value("Thumbnail V1"));
    }

    @Test
    void updateAsset_Forbidden_ContributorOthers() throws Exception {
        mockAuth(contributorUser);
        doThrow(new AccessDeniedException("Access denied: Contributors can only modify assets they created."))
                .when(assetService).updateAsset(eq(500L), eq("bruce@slay.com"), any(AssetRequest.class));

        mockMvc.perform(put("/api/assets/500")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void deleteAsset_Success() throws Exception {
        mockAuth(managerUser);
        doNothing().when(assetService).deleteAsset(eq(500L), eq("tony@slay.com"));

        mockMvc.perform(delete("/api/assets/500")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAsset_Forbidden_ContributorOthers() throws Exception {
        mockAuth(contributorUser);
        doThrow(new AccessDeniedException("Access denied: Contributors can only delete assets they created."))
                .when(assetService).deleteAsset(eq(500L), eq("bruce@slay.com"));

        mockMvc.perform(delete("/api/assets/500")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
