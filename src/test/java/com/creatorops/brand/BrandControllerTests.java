package com.creatorops.brand;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.brand.dto.BrandRequest;
import com.creatorops.brand.dto.BrandResponse;
import com.creatorops.brand.service.BrandService;
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
class BrandControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BrandService brandService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User adminUser;
    private User managerUser;
    private Organization organization;
    private BrandRequest brandRequest;
    private BrandResponse brandResponse;

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

        brandRequest = new BrandRequest("SLAY Fashion", "Fashion tips", "https://example.com/logo.png");
        brandResponse = new BrandResponse(10L, "SLAY Fashion", "Fashion tips", "https://example.com/logo.png", 1L);
    }

    private void mockAuth(User user) {
        when(jwtService.extractUsername(anyString())).thenReturn(user.getEmail());
        when(jwtService.isTokenValid(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void getBrands_Success() throws Exception {
        mockAuth(managerUser);
        when(brandService.getBrands(eq("rogers@slay.com"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(brandResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/brands")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].name").value("SLAY Fashion"))
                .andExpect(jsonPath("$.pagination.totalPages").value(1));
    }

    @Test
    void createBrand_AdminSuccess() throws Exception {
        mockAuth(adminUser);
        when(brandService.createBrand(eq("tony@slay.com"), any(BrandRequest.class)))
                .thenReturn(brandResponse);

        mockMvc.perform(post("/api/brands")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("SLAY Fashion"));
    }

    @Test
    void createBrand_ManagerForbidden() throws Exception {
        mockAuth(managerUser);

        mockMvc.perform(post("/api/brands")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateBrand_AdminSuccess() throws Exception {
        mockAuth(adminUser);
        when(brandService.updateBrand(eq(10L), any(BrandRequest.class), eq("tony@slay.com")))
                .thenReturn(brandResponse);

        mockMvc.perform(put("/api/brands/10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void deleteBrand_AdminSuccess() throws Exception {
        mockAuth(adminUser);
        doNothing().when(brandService).deleteBrand(eq(10L), eq("tony@slay.com"));

        mockMvc.perform(delete("/api/brands/10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
