package org.example.storemanager.controller.auth;

import jakarta.validation.Valid;
import org.example.storemanager.dto.request.auth.ChangePasswordRequest;
import org.example.storemanager.dto.request.auth.LoginRequest;
import org.example.storemanager.dto.request.auth.RefreshTokenRequest;
import org.example.storemanager.dto.request.auth.RegisterRequest;
import org.example.storemanager.dto.response.auth.LoginResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/v1/auth/register
     * Đăng ký tài khoản mới — trả về access token + refresh token ngay lập tức.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    /**
     * POST /api/v1/auth/login
     * Đăng nhập — trả về access token (1h) + refresh token (7 ngày).
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * POST /api/v1/auth/refresh
     * Dùng refresh token để lấy access token mới (Refresh Token Rotation).
     * Refresh token cũ sẽ bị thu hồi và một cặp token mới được cấp.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * POST /api/v1/auth/logout
     * Đăng xuất — thu hồi refresh token hiện tại.
     * Gửi refresh token trong body.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    /**
     * POST /api/v1/auth/logout-all
     * Đăng xuất khỏi tất cả thiết bị — thu hồi mọi refresh token của user.
     * Yêu cầu: phải có access token hợp lệ trong header Authorization.
     */
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Chưa đăng nhập"));
        }
        authService.logoutAll(auth.getName());
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    /**
     * POST /api/v1/auth/change-password
     * Đổi mật khẩu — yêu cầu access token hợp lệ.
     * Sau khi đổi mật khẩu, tất cả refresh token sẽ bị thu hồi.
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Chưa đăng nhập"));
        }
        authService.changePassword(auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
