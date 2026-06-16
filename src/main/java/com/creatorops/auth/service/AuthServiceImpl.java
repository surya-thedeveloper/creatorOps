package com.creatorops.auth.service;

import com.creatorops.auth.dto.*;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.exception.InvalidCredentialsException;
import com.creatorops.auth.exception.UserAlreadyExistsException;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.common.exception.ResourceNotFoundException;
import com.creatorops.organization.entity.Organization;
import com.creatorops.organization.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           OrganizationRepository organizationRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        Organization organization = organizationRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Default organization not found"));

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.name(), request.email(), encodedPassword);
        user.setRole(UserRole.ADMIN); // Default fallback role
        user.setOrganization(organization);

        User savedUser = userRepository.save(user);
        org.slf4j.MDC.put("entityId", String.valueOf(savedUser.getId()));
        log.info("User registered successfully: email={}, role={}", savedUser.getEmail(), savedUser.getRole());
        org.slf4j.MDC.remove("entityId");
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getName(),
                user.getRole().name(),
                user.getOrganizationId()
        );
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        org.slf4j.MDC.put("entityId", String.valueOf(user.getId()));
        log.info("User logged in successfully: email={}", user.getEmail());
        org.slf4j.MDC.remove("entityId");

        return new LoginResponse(accessToken, refreshToken, UserResponse.fromEntity(user));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'dto-' + #email")
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found associated with token"));

        String newAccessToken = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getName(),
                user.getRole().name(),
                user.getOrganizationId()
        );
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.email()));

        String token = java.util.UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpiry(OffsetDateTime.now().plusHours(1));

        userRepository.save(user);
        org.slf4j.MDC.put("entityId", String.valueOf(user.getId()));
        log.info("Password reset initiated for user: email={}", user.getEmail());
        org.slf4j.MDC.remove("entityId");
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.token())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired reset token"));

        if (user.getPasswordResetExpiry() == null || user.getPasswordResetExpiry().isBefore(OffsetDateTime.now())) {
            throw new InvalidCredentialsException("Invalid or expired reset token");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);

        userRepository.save(user);
        org.slf4j.MDC.put("entityId", String.valueOf(user.getId()));
        log.info("Password reset executed successfully for user: email={}", user.getEmail());
        org.slf4j.MDC.remove("entityId");
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#email"),
        @CacheEvict(value = "users", key = "'dto-' + #email")
    })
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Old password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        org.slf4j.MDC.put("entityId", String.valueOf(user.getId()));
        log.info("Password changed successfully for user: email={}", user.getEmail());
        org.slf4j.MDC.remove("entityId");
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#email"),
        @CacheEvict(value = "users", key = "'dto-' + #email")
    })
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setName(request.name());
        user.setImageUrl(request.imageUrl());

        User updatedUser = userRepository.save(user);
        org.slf4j.MDC.put("entityId", String.valueOf(updatedUser.getId()));
        log.info("User profile updated successfully: email={}", updatedUser.getEmail());
        org.slf4j.MDC.remove("entityId");
        return UserResponse.fromEntity(updatedUser);
    }
}

