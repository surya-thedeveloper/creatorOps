package com.creatorops.auth.controller;

import com.creatorops.auth.dto.ChangePasswordRequest;
import com.creatorops.auth.dto.UpdateProfileRequest;
import com.creatorops.auth.dto.UserResponse;
import com.creatorops.auth.service.AuthService;
import com.creatorops.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Profile", description = "Manage the authenticated user's profile and password. All endpoints require a valid Bearer token.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final AuthService authService;

    @Autowired
    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get my profile", description = "Returns full profile for the currently authenticated user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile returned successfully")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        UserResponse response = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Profile retrieved successfully"));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update my profile", description = "Updates the name and/or avatar image URL for the current user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        UserResponse response = authService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes the current user's password. Requires the current password for verification.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
}
