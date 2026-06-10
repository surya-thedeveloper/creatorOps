package com.creatorops.auth.service;

import com.creatorops.auth.dto.*;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    UserResponse getCurrentUser(String email);
    TokenRefreshResponse refresh(TokenRefreshRequest request);
    void initiatePasswordReset(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void changePassword(String email, ChangePasswordRequest request);
    UserResponse updateProfile(String email, UpdateProfileRequest request);
}
