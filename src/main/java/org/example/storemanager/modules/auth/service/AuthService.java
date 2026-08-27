package org.example.storemanager.modules.auth.service;

import   org.example.storemanager.modules.auth.dto.request.ChangePasswordRequest;
import   org.example.storemanager.modules.auth.dto.request.RegisterRequest;
import   org.example.storemanager.modules.auth.dto.request.LoginRequest;
import   org.example.storemanager.modules.auth.dto.request.RefreshTokenRequest;

import org.example.storemanager.modules.auth.dto.response.LoginResponse;

import java.util.List;

public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse registerCustomer(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void logout(String refreshToken);
    void logoutAll(String username);
    void changePassword(String username, ChangePasswordRequest request);
    List<String> getMyPermissions(String username);
    org.example.storemanager.modules.auth.dto.response.UserInfoResponse updateProfile(String username, org.example.storemanager.modules.auth.dto.request.UpdateProfileRequest request);
    org.example.storemanager.modules.auth.dto.response.UserInfoResponse getProfile(String username);
}
