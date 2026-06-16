package com.creatorops.auth;

import com.creatorops.auth.dto.*;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.exception.InvalidCredentialsException;
import com.creatorops.auth.exception.UserAlreadyExistsException;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.auth.service.AuthServiceImpl;
import com.creatorops.organization.entity.Organization;
import com.creatorops.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private Organization organization;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("Surya", "surya@example.com", "SecurePassword123");
        loginRequest = new LoginRequest("surya@example.com", "SecurePassword123");
        organization = new Organization("Default Organization", null);
        organization.setId(1L);
        user = new User("Surya", "surya@example.com", "hashed_password");
        user.setId(1L);
        user.setRole(UserRole.ADMIN);
        user.setOrganization(organization);
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("Surya", response.name());
        assertEquals("surya@example.com", response.email());
        assertEquals("ADMIN", response.role());
        assertEquals(1L, response.organizationId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any(), any(), any())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh_token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertEquals("refresh_token", response.refreshToken());
        assertEquals("Surya", response.user().name());
        assertEquals("ADMIN", response.user().role());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void initiatePasswordReset_Success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("surya@example.com");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        authService.initiatePasswordReset(request);

        assertNotNull(user.getPasswordResetToken());
        assertNotNull(user.getPasswordResetExpiry());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void resetPassword_Success() {
        user.setPasswordResetToken("reset_token");
        user.setPasswordResetExpiry(java.time.OffsetDateTime.now().plusHours(1));

        ResetPasswordRequest request = new ResetPasswordRequest("reset_token", "NewSecurePassword123");
        when(userRepository.findByPasswordResetToken("reset_token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewSecurePassword123")).thenReturn("new_hashed_password");

        authService.resetPassword(request);

        assertEquals("new_hashed_password", user.getPasswordHash());
        assertNull(user.getPasswordResetToken());
        assertNull(user.getPasswordResetExpiry());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void resetPassword_ExpiredToken_ThrowsException() {
        user.setPasswordResetToken("expired_token");
        user.setPasswordResetExpiry(java.time.OffsetDateTime.now().minusHours(1));

        ResetPasswordRequest request = new ResetPasswordRequest("expired_token", "NewSecurePassword123");
        when(userRepository.findByPasswordResetToken("expired_token")).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> authService.resetPassword(request));
        verify(userRepository, never()).save(user);
    }

    @Test
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest("SecurePassword123", "NewSecurePassword123");
        when(userRepository.findByEmail("surya@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecurePassword123", user.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode("NewSecurePassword123")).thenReturn("new_hashed_password");

        authService.changePassword("surya@example.com", request);

        assertEquals("new_hashed_password", user.getPasswordHash());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changePassword_InvalidOldPassword_ThrowsException() {
        ChangePasswordRequest request = new ChangePasswordRequest("WrongPassword", "NewSecurePassword123");
        when(userRepository.findByEmail("surya@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", user.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.changePassword("surya@example.com", request));
        verify(userRepository, never()).save(user);
    }

    @Test
    void updateProfile_Success() {
        UpdateProfileRequest request = new UpdateProfileRequest("New Name", "https://example.com/new.png");
        when(userRepository.findByEmail("surya@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = authService.updateProfile("surya@example.com", request);

        assertNotNull(response);
        assertEquals("New Name", response.name());
        assertEquals("https://example.com/new.png", response.imageUrl());
        verify(userRepository, times(1)).save(user);
    }
}

