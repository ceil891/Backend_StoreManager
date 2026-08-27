package org.example.storemanager.modules.auth.service;

import org.example.storemanager.modules.auth.dto.request.ChangePasswordRequest;
import org.example.storemanager.modules.auth.dto.request.LoginRequest;
import org.example.storemanager.modules.auth.dto.request.RefreshTokenRequest;
import org.example.storemanager.modules.auth.dto.request.RegisterRequest;
import org.example.storemanager.modules.auth.dto.response.LoginResponse;
import org.example.storemanager.modules.marketing.repository.CustomerVoucherRepository;
import org.example.storemanager.modules.marketing.repository.VoucherRepository;
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.system.entity.RefreshToken;
import org.example.storemanager.modules.system.entity.Role;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.modules.system.repository.RefreshTokenRepository;
import org.example.storemanager.modules.system.repository.RolePermissionRepository;
import org.example.storemanager.modules.system.repository.RoleRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.example.storemanager.shared.exception.BusinessException;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.JwtAuthenticationException;
import org.example.storemanager.shared.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private CustomerVoucherRepository customerVoucherRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private Role sampleRole;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        sampleRole = Role.builder()
                .roleName("USER")
                .description("Default User")
                .build();
        sampleRole.setId(1L);

        sampleUser = User.builder()
                .username("testuser")
                .password(encoder.encode("secret123"))
                .fullName("Nguyễn Văn Test")
                .email("testuser@example.com")
                .phone("0901234567")
                .role(sampleRole)
                .status("ACTIVE")
                .build();
        sampleUser.setId(100L);
    }

    @Nested
    @DisplayName("1. Đăng ký tài khoản (Register)")
    class RegisterTests {

        @Test
        @DisplayName("Đăng ký thành công - trả về LoginResponse với token hợp lệ")
        void register_Success() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("newuser");
            req.setPassword("password123");
            req.setFullName("User Mới");
            req.setEmail("newuser@example.com");
            req.setPhone("0987654321");

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
            when(userRepository.existsByPhone("0987654321")).thenReturn(false);
            when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(sampleRole));
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            when(jwtUtil.generateAccessToken(anyString())).thenReturn("mock-access-token");
            when(jwtUtil.generateRefreshToken(anyString())).thenReturn("mock-refresh-token");
            when(jwtUtil.getRefreshTokenExpirationMs()).thenReturn(604800000L);

            LoginResponse response = authService.register(req);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
            verify(userRepository, times(1)).save(any(User.class));
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Đăng ký thất bại khi trùng Username - throw DuplicateResourceException")
        void register_DuplicateUsername_ThrowsException() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("testuser");
            req.setPassword("password123");

            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> authService.register(req));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Đăng ký thất bại khi trùng Email - throw DuplicateResourceException")
        void register_DuplicateEmail_ThrowsException() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("uniqueuser");
            req.setEmail("existing@example.com");
            req.setPassword("password123");

            when(userRepository.existsByUsername("uniqueuser")).thenReturn(false);
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> authService.register(req));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("2. Đăng nhập (Login)")
    class LoginTests {

        @Test
        @DisplayName("Đăng nhập thành công với Username & Password đúng")
        void login_Success() {
            LoginRequest req = new LoginRequest();
            req.setUsername("testuser");
            req.setPassword("secret123");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
            when(jwtUtil.generateAccessToken(sampleUser.getUsername())).thenReturn("access-token-123");
            when(jwtUtil.generateRefreshToken(sampleUser.getUsername())).thenReturn("refresh-token-123");
            when(jwtUtil.getRefreshTokenExpirationMs()).thenReturn(604800000L);
            when(rolePermissionRepository.findByRoleId(sampleRole.getId())).thenReturn(Collections.emptyList());

            LoginResponse res = authService.login(req);

            assertThat(res).isNotNull();
            assertThat(res.getAccessToken()).isEqualTo("access-token-123");
            assertThat(res.getRefreshToken()).isEqualTo("refresh-token-123");
            assertThat(res.getUser().getName()).isEqualTo("Nguyễn Văn Test");
        }

        @Test
        @DisplayName("Đăng nhập thất bại khi tài khoản không tồn tại")
        void login_UserNotFound_ThrowsBusinessException() {
            LoginRequest req = new LoginRequest();
            req.setUsername("nonexistent");
            req.setPassword("secret123");

            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> authService.login(req));
        }

        @Test
        @DisplayName("Đăng nhập thất bại khi sai mật khẩu")
        void login_WrongPassword_ThrowsBusinessException() {
            LoginRequest req = new LoginRequest();
            req.setUsername("testuser");
            req.setPassword("wrongpassword");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

            assertThrows(BusinessException.class, () -> authService.login(req));
        }

        @Test
        @DisplayName("Đăng nhập thất bại khi tài khoản bị khóa (LOCKED)")
        void login_AccountLocked_ThrowsBusinessException() {
            sampleUser.setStatus("LOCKED");
            LoginRequest req = new LoginRequest();
            req.setUsername("testuser");
            req.setPassword("secret123");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

            assertThrows(BusinessException.class, () -> authService.login(req));
        }
    }

    @Nested
    @DisplayName("3. Làm mới Token (Refresh Token)")
    class RefreshTokenTests {

        @Test
        @DisplayName("Refresh Token thành công khi token hợp lệ và chưa bị thu hồi")
        void refreshToken_Success() {
            String rawToken = "valid-refresh-token";
            RefreshTokenRequest req = new RefreshTokenRequest();
            req.setRefreshToken(rawToken);

            RefreshToken stored = RefreshToken.builder()
                    .token(rawToken)
                    .user(sampleUser)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .isRevoked(false)
                    .build();

            when(jwtUtil.isTokenValid(rawToken)).thenReturn(true);
            when(refreshTokenRepository.findByToken(rawToken)).thenReturn(Optional.of(stored));
            when(jwtUtil.generateAccessToken(sampleUser.getUsername())).thenReturn("new-access-token");
            when(jwtUtil.generateRefreshToken(sampleUser.getUsername())).thenReturn("new-refresh-token");
            when(jwtUtil.getRefreshTokenExpirationMs()).thenReturn(604800000L);

            LoginResponse res = authService.refreshToken(req);

            assertThat(res.getAccessToken()).isEqualTo("new-access-token");
            assertThat(stored.getIsRevoked()).isTrue(); // Token rotation
            verify(refreshTokenRepository, atLeastOnce()).save(stored);
        }

        @Test
        @DisplayName("Refresh Token thất bại khi token JWT không hợp lệ")
        void refreshToken_InvalidJwt_ThrowsJwtException() {
            String rawToken = "malformed-token";
            RefreshTokenRequest req = new RefreshTokenRequest();
            req.setRefreshToken(rawToken);

            when(jwtUtil.isTokenValid(rawToken)).thenReturn(false);

            assertThrows(JwtAuthenticationException.class, () -> authService.refreshToken(req));
        }
    }

    @Nested
    @DisplayName("4. Đổi mật khẩu (Change Password)")
    class ChangePasswordTests {

        @Test
        @DisplayName("Đổi mật khẩu thành công và thu hồi tất cả phiên đăng nhập cũ")
        void changePassword_Success() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setOldPassword("secret123");
            req.setNewPassword("newPassword456");
            req.setConfirmPassword("newPassword456");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

            authService.changePassword("testuser", req);

            verify(userRepository, times(1)).save(sampleUser);
            verify(refreshTokenRepository, times(1)).revokeAllByUser(sampleUser);
        }

        @Test
        @DisplayName("Đổi mật khẩu thất bại khi mật khẩu cũ không đúng")
        void changePassword_WrongOldPassword_ThrowsException() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setOldPassword("wrongOldPass");
            req.setNewPassword("newPassword456");
            req.setConfirmPassword("newPassword456");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

            assertThrows(BusinessException.class, () -> authService.changePassword("testuser", req));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Đổi mật khẩu thất bại khi xác nhận mật khẩu không khớp")
        void changePassword_PasswordMismatch_ThrowsException() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setOldPassword("secret123");
            req.setNewPassword("newPassword456");
            req.setConfirmPassword("differentPassword");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

            assertThrows(BusinessException.class, () -> authService.changePassword("testuser", req));
        }
    }
}
