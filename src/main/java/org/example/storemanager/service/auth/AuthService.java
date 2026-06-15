package org.example.storemanager.service.auth;

import   org.example.storemanager.dto.request.auth.ChangePasswordRequest;
import   org.example.storemanager.dto.request.auth.RegisterRequest;
import   org.example.storemanager.dto.request.auth.LoginRequest;
import   org.example.storemanager.dto.request.auth.RefreshTokenRequest;

import org.example.storemanager.dto.response.auth.LoginResponse;

public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void logout(String refreshToken);
    void logoutAll(String username);
    void changePassword(String username, ChangePasswordRequest request);
}
