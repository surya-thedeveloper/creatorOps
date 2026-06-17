package com.creatorops.auth.controller;

import com.creatorops.auth.dto.*;
import com.creatorops.auth.service.AuthService;
import com.creatorops.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register, login, token refresh, and password management. Auth endpoints do not require a Bearer token.")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account. Returns user profile and sets initial CONTRIBUTOR role.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or email already in use"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "User registered successfully"),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and obtain JWT tokens", description = "Returns an access token (15 min) and a refresh token (7 days). Use the access token as: Authorization: Bearer <token>")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful — tokens returned")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Exchange a valid refresh token for a new access token without re-logging in.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "New access token issued")
    public ResponseEntity<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", description = "Returns the profile of the user identified by the Bearer token.")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        String email = authentication.getName();
        UserResponse response = authService.getCurrentUser(email);
        return ResponseEntity.ok(ApiResponse.success(response, "Current user retrieved successfully"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password reset", description = "Generates a password reset token for the given email. In production, the token would be emailed.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset token generated")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.initiatePasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset token has been generated successfully"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token", description = "Resets the user's password using the token from the forgot-password flow.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password has been reset successfully"));
    }
}
