package com.creatorops.auth;

import com.creatorops.auth.controller.UserController;
import com.creatorops.auth.dto.*;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.auth.service.AuthService;
import com.creatorops.organization.entity.Organization;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse(1L, "Surya", "surya@example.com", "ADMIN", "https://example.com/avatar.png", 1L);
    }

    private void mockAuthentication() {
        Organization organization = new Organization("Default Organization", null);
        organization.setId(1L);
        User user = new User("Surya", "surya@example.com", "hashed_password");
        user.setId(1L);
        user.setRole(UserRole.ADMIN);
        user.setOrganization(organization);

        when(jwtService.extractUsername(anyString())).thenReturn("surya@example.com");
        when(jwtService.isTokenValid(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByEmail("surya@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void getProfile_Success() throws Exception {
        mockAuthentication();
        when(authService.getCurrentUser("surya@example.com")).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("surya@example.com"))
                .andExpect(jsonPath("$.data.name").value("Surya"));
    }

    @Test
    void getProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_Success() throws Exception {
        mockAuthentication();
        UpdateProfileRequest request = new UpdateProfileRequest("New Name", "https://example.com/new.png");
        UserResponse updatedResponse = new UserResponse(1L, "New Name", "surya@example.com", "ADMIN", "https://example.com/new.png", 1L);

        when(authService.updateProfile(eq("surya@example.com"), any(UpdateProfileRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/users/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("New Name"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/new.png"));
    }

    @Test
    void changePassword_Success() throws Exception {
        mockAuthentication();
        ChangePasswordRequest request = new ChangePasswordRequest("OldSecurePassword", "NewSecurePassword123");

        doNothing().when(authService).changePassword(eq("surya@example.com"), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/api/v1/users/change-password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    void forgotPassword_Success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("surya@example.com");

        doNothing().when(authService).initiatePasswordReset(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset token has been generated successfully"));
    }

    @Test
    void resetPassword_Success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token-value", "NewSecurePassword123");

        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password has been reset successfully"));
    }
}
