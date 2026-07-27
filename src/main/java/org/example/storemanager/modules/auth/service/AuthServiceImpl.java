package org.example.storemanager.modules.auth.service;

import org.example.storemanager.modules.auth.dto.request.ChangePasswordRequest;
import org.example.storemanager.modules.auth.dto.request.LoginRequest;
import org.example.storemanager.modules.auth.dto.request.RefreshTokenRequest;
import org.example.storemanager.modules.auth.dto.request.RegisterRequest;
import org.example.storemanager.modules.auth.dto.response.LoginResponse;
import org.example.storemanager.modules.auth.dto.response.UserInfoResponse;
import org.example.storemanager.modules.system.entity.RefreshToken;
import org.example.storemanager.modules.system.entity.RolePermission;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.shared.enums.ErrorCode;
import org.example.storemanager.shared.exception.BusinessException;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.JwtAuthenticationException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.system.repository.RefreshTokenRepository;
import org.example.storemanager.modules.system.repository.RolePermissionRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.example.storemanager.shared.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           RolePermissionRepository rolePermissionRepository,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ==================== ĐĂNG KÝ ====================

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Kiểm tra trùng username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Tài khoản", "username", request.getUsername());
        }
        // Kiểm tra trùng email
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Tài khoản", "email", request.getEmail());
        }
        // Kiểm tra trùng số điện thoại
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Tài khoản", "phone", request.getPhone());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status("ACTIVE")
                .build();
        user.setCreatedBy(request.getUsername());

        User saved = userRepository.save(user);

        // Tự động đăng nhập sau khi đăng ký
        return issueTokenPair(saved);
    }

    // ==================== ĐĂNG NHẬP ====================

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String input = request.getUsername();
        User user = userRepository.findByUsername(input)
                .or(() -> userRepository.findByEmail(input))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS,
                        "Tên đăng nhập hoặc mật khẩu không đúng"));

        // Kiểm tra trạng thái tài khoản
        if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED, null);
        }
        if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, null);
        }

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS,
                    "Tên đăng nhập hoặc mật khẩu không đúng");
        }

        return issueTokenPair(user);
    }

    // ==================== REFRESH TOKEN ====================

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenStr = request.getRefreshToken();

        // Kiểm tra chữ ký + hết hạn
        if (!jwtUtil.isTokenValid(refreshTokenStr)) {
            throw new JwtAuthenticationException(ErrorCode.TOKEN_EXPIRED, "Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // Tìm trong DB
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new JwtAuthenticationException(ErrorCode.INVALID_TOKEN, "Refresh token không tồn tại"));

        // Kiểm tra đã bị thu hồi chưa
        if (Boolean.TRUE.equals(storedToken.getIsRevoked())) {
            throw new JwtAuthenticationException(ErrorCode.INVALID_TOKEN, "Refresh token đã bị thu hồi");
        }

        // Kiểm tra hết hạn trong DB
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            storedToken.setIsRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new JwtAuthenticationException(ErrorCode.TOKEN_EXPIRED, "Refresh token đã hết hạn");
        }

        // Thu hồi refresh token cũ (Rotation)
        storedToken.setIsRevoked(true);
        refreshTokenRepository.save(storedToken);

        // Cấp token mới
        User user = storedToken.getUser();
        return issueTokenPair(user);
    }

    // ==================== ĐĂNG XUẤT ====================

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
            rt.setIsRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    @Transactional
    public void logoutAll(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "Người dùng", "username", username));
        refreshTokenRepository.revokeAllByUser(user);
    }

    // ==================== ĐỔI MẬT KHẨU ====================

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "Người dùng", "username", username));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Mật khẩu cũ không đúng");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedBy(username);
        userRepository.save(user);

        // Thu hồi tất cả refresh token sau khi đổi mật khẩu
        refreshTokenRepository.revokeAllByUser(user);
    }

    // ==================== LẤY QUYỀN HIỆN TẠI ====================

    @Override
    @Transactional(readOnly = true)
    public List<String> getMyPermissions(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getRole() == null) {
            return Collections.emptyList();
        }
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(user.getRole().getId());
        return rolePermissions.stream()
                .map(rp -> rp.getPermission().getPermissionCode())
                .collect(Collectors.toList());
    }

    // ==================== HELPER ====================

    /**
     * Tạo access token + refresh token mới và lưu refresh token vào DB.
     */
    private LoginResponse issueTokenPair(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshTokenStr = jwtUtil.generateRefreshToken(user.getUsername());

        // Lưu refresh token vào DB
        long expiryMs = jwtUtil.getRefreshTokenExpirationMs();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(expiryMs / 1000))
                .isRevoked(false)
                .build();
        refreshToken.setCreatedBy(user.getUsername());
        refreshTokenRepository.save(refreshToken);

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "USER";

        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getFullName())
                .email(user.getEmail())
                .role(roleName)
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().toString() : null)
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .user(userInfo)
                .build();
    }
}
