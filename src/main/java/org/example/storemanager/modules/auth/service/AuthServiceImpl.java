package org.example.storemanager.modules.auth.service;

import org.example.storemanager.modules.auth.dto.request.ChangePasswordRequest;
import org.example.storemanager.modules.auth.dto.request.ForgotPasswordRequest;
import org.example.storemanager.modules.auth.dto.request.LoginRequest;
import org.example.storemanager.modules.auth.dto.request.RefreshTokenRequest;
import org.example.storemanager.modules.auth.dto.request.RegisterRequest;
import org.example.storemanager.modules.auth.dto.request.ResetPasswordRequest;
import org.example.storemanager.modules.auth.dto.request.VerifyOtpRequest;
import org.example.storemanager.modules.auth.dto.response.LoginResponse;
import org.example.storemanager.modules.auth.dto.response.UserInfoResponse;
import org.example.storemanager.modules.system.entity.RefreshToken;
import org.example.storemanager.modules.system.entity.Role;
import org.example.storemanager.modules.system.entity.RolePermission;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.shared.enums.ErrorCode;
import org.example.storemanager.shared.exception.BusinessException;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.JwtAuthenticationException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.system.repository.RefreshTokenRepository;
import org.example.storemanager.modules.system.repository.RolePermissionRepository;
import org.example.storemanager.modules.system.repository.RoleRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.marketing.entity.Voucher;
import org.example.storemanager.modules.marketing.repository.VoucherRepository;
import org.example.storemanager.modules.marketing.entity.CustomerVoucher;
import org.example.storemanager.modules.marketing.repository.CustomerVoucherRepository;
import org.example.storemanager.shared.security.JwtUtil;
import org.example.storemanager.shared.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final VoucherRepository voucherRepository;
    private final CustomerVoucherRepository customerVoucherRepository;
    private final EmailService emailService;

    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();

    private record OtpEntry(String otp, LocalDateTime expiresAt) {}

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           RolePermissionRepository rolePermissionRepository,
                           RoleRepository roleRepository,
                           JwtUtil jwtUtil,
                           CustomerRepository customerRepository,
                           VoucherRepository voucherRepository,
                           CustomerVoucherRepository customerVoucherRepository,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.customerRepository = customerRepository;
        this.voucherRepository = voucherRepository;
        this.customerVoucherRepository = customerVoucherRepository;
        this.emailService = emailService;
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

        Role defaultRole = roleRepository.findByRoleName("USER")
                .or(() -> roleRepository.findByRoleName("Nguoi dung"))
                .or(() -> roleRepository.findByRoleName("Người dùng"))
                .or(() -> roleRepository.findByRoleName("Khách hàng"))
                .orElse(null);

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(defaultRole)
                .status("ACTIVE")
                .build();
        user.setCreatedBy(request.getUsername());

        User saved = userRepository.save(user);

        // Tự động đăng nhập sau khi đăng ký
        return issueTokenPair(saved);
    }

    @Override
    @Transactional
    public LoginResponse registerCustomer(RegisterRequest request) {
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

        Role defaultRole = roleRepository.findByRoleName("CUSTOMER")
                .or(() -> roleRepository.findByRoleName("Khách hàng"))
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .roleName("CUSTOMER")
                            .description("Khách hàng mua sắm Web Online")
                            .isActive(true)
                            .build();
                    newRole.setIsDeleted(false);
                    return roleRepository.save(newRole);
                });

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(defaultRole)
                .status("ACTIVE")
                .build();
        user.setCreatedBy(request.getUsername());

        User savedUser = userRepository.save(user);

        // 1. Automatically create Customer
        Customer customer = Customer.builder()
                .customerCode("CUST-" + java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .name(savedUser.getFullName() != null ? savedUser.getFullName() : savedUser.getUsername())
                .phone(savedUser.getPhone())
                .email(savedUser.getEmail())
                .isActive(true)
                .points(0.0)
                .totalSpend(0.0)
                .membershipRank("Đồng")
                .build();
        customer.setCreatedBy("SYSTEM");
        Customer savedCustomer = customerRepository.save(customer);

        // 2. Automatically create & issue WELCOME voucher (NEW2026)
        Voucher v = voucherRepository.findByVoucherCode("NEW2026").orElse(null);
        if (v == null) {
            v = Voucher.builder()
                    .voucherCode("NEW2026")
                    .voucherName("Chào bạn mới")
                    .type("PERCENTAGE")
                    .value(new java.math.BigDecimal("10"))
                    .maxUsage(100)
                    .currentUsage(0)
                    .description("Voucher chào mừng thành viên mới giảm 10%")
                    .minOrderAmount(java.math.BigDecimal.ZERO)
                    .maxDiscountAmount(new java.math.BigDecimal("50000"))
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(30))
                    .status("ACTIVE")
                    .isPublic(true)
                    .isActive(true)
                    .build();
            v.setIsDeleted(false);
            v = voucherRepository.save(v);
        }

        CustomerVoucher cv = CustomerVoucher.builder()
                .customer(savedCustomer)
                .voucher(v)
                .voucherCode(v.getVoucherCode())
                .collectedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(30))
                .status("ACTIVE")
                .build();
        cv.setIsDeleted(false);
        customerVoucherRepository.save(cv);

        // Gửi email chào mừng khách hàng kèm mã voucher ưu đãi
        if (savedUser.getEmail() != null && !savedUser.getEmail().isBlank()) {
            emailService.sendWelcomeCustomerEmail(savedUser.getEmail(), savedUser.getFullName(), savedUser.getUsername(), "NEW2026");
        }

        return issueTokenPair(savedUser);
    }

    // ==================== ĐĂNG NHẬP ====================

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String input = request.getUsername();
        log.info("[AuthService] Step 1: Searching for user with input: [{}]", input);
        User user = userRepository.findByUsername(input)
                .or(() -> userRepository.findByEmail(input))
                .orElse(null);

        if (user == null) {
            log.warn("[AuthService] User NOT FOUND in database for input: [{}]", input);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS,
                    "Tên đăng nhập hoặc mật khẩu không đúng");
        }

        log.info("[AuthService] Step 2: Found user [id={}, username={}, email={}, status={}]", 
                user.getId(), user.getUsername(), user.getEmail(), user.getStatus());

        // Kiểm tra trạng thái tài khoản User
        if ("LOCKED".equalsIgnoreCase(user.getStatus()) || "SUSPENDED".equalsIgnoreCase(user.getStatus()) || "TERMINATED".equalsIgnoreCase(user.getStatus())) {
            log.warn("[AuthService] User [{}] is LOCKED/SUSPENDED", input);
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED, "Tài khoản của bạn đã bị khóa hoặc tạm ngừng hoạt động. Vui lòng liên hệ quản trị viên.");
        }
        if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
            log.warn("[AuthService] User [{}] is INACTIVE", input);
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "Tài khoản này hiện đang bị vô hiệu hóa.");
        }

        // Kiểm tra mật khẩu
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matches) {
            log.warn("[AuthService] Password does NOT match for user [{}]", input);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS,
                    "Tên đăng nhập hoặc mật khẩu không đúng");
        }

        log.info("[AuthService] Step 3: Password matched! Issuing token pair for user [{}]", input);
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
        try {
            refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
                if (Boolean.TRUE.equals(rt.getIsRevoked())) return; // already revoked
                rt.setIsRevoked(true);
                refreshTokenRepository.save(rt);
            });
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
            // Token was concurrently revoked by another request — treat as success
            log.warn("Logout: token already revoked by concurrent request, ignoring: {}", ex.getMessage());
        }
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

    // ==================== CẬP NHẬT HỒ SƠ & AVATAR ====================

    @Override
    @Transactional
    public UserInfoResponse updateProfile(String username, org.example.storemanager.modules.auth.dto.request.UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng: " + username));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().trim());
        }
        user.setUpdatedBy(username);
        User savedUser = userRepository.save(user);

        // Đồng bộ với Customer nếu có
        try {
            if (savedUser.getEmail() != null && !savedUser.getEmail().isBlank()) {
                Customer cust = customerRepository.findByEmailAndIsDeletedFalse(savedUser.getEmail()).orElse(null);
                if (cust != null) {
                    if (request.getFullName() != null && !request.getFullName().isBlank()) {
                        cust.setName(request.getFullName().trim());
                    }
                    if (request.getPhone() != null && !request.getPhone().isBlank()) {
                        cust.setPhone(request.getPhone().trim());
                    }
                    if (request.getAvatar() != null) {
                        cust.setAvatarUrl(request.getAvatar().trim());
                    }
                    customerRepository.save(cust);
                }
            }
        } catch (Exception e) {
            log.warn("[AuthService] Failed to sync customer profile: {}", e.getMessage());
        }

        String roleName = savedUser.getRole() != null ? savedUser.getRole().getRoleName() : "USER";
        List<String> permissions = Collections.emptyList();
        if (savedUser.getRole() != null) {
            if ("SUPER_ADMIN".equalsIgnoreCase(roleName)) {
                permissions = List.of("*");
            } else {
                Set<String> permCodes = rolePermissionRepository.findPermissionCodesByRoleId(savedUser.getRole().getId());
                permissions = permCodes != null ? new ArrayList<>(permCodes) : Collections.emptyList();
            }
        }

        return UserInfoResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(roleName)
                .branchId(savedUser.getBranch() != null ? savedUser.getBranch().getId() : null)
                .branchCode(savedUser.getBranch() != null ? savedUser.getBranch().getBranchCode() : null)
                .branchName(savedUser.getBranch() != null ? savedUser.getBranch().getBranchName() : null)
                .avatar(savedUser.getAvatar())
                .permissions(permissions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng: " + username));

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "USER";
        List<String> permissions = Collections.emptyList();
        if (user.getRole() != null) {
            if ("SUPER_ADMIN".equalsIgnoreCase(roleName)) {
                permissions = List.of("*");
            } else {
                Set<String> permCodes = rolePermissionRepository.findPermissionCodesByRoleId(user.getRole().getId());
                permissions = permCodes != null ? new ArrayList<>(permCodes) : Collections.emptyList();
            }
        }

        return UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getFullName())
                .email(user.getEmail())
                .role(roleName)
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchCode(user.getBranch() != null ? user.getBranch().getBranchCode() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getBranchName() : null)
                .avatar(user.getAvatar())
                .permissions(permissions)
                .build();
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

        // Lấy danh sách quyền từ vai trò của user (Tối ưu 1 query JOIN duy nhất)
        List<String> permissions = Collections.emptyList();
        if (user.getRole() != null) {
            if ("SUPER_ADMIN".equalsIgnoreCase(roleName)) {
                permissions = List.of("*");
            } else {
                Set<String> permCodes = rolePermissionRepository.findPermissionCodesByRoleId(user.getRole().getId());
                permissions = permCodes != null ? new ArrayList<>(permCodes) : Collections.emptyList();
            }
        }

        String userAvatar = user.getAvatar();
        if ((userAvatar == null || userAvatar.isBlank()) && user.getEmail() != null && !user.getEmail().isBlank()) {
            Customer cust = customerRepository.findByEmailAndIsDeletedFalse(user.getEmail()).orElse(null);
            if (cust != null && cust.getAvatarUrl() != null && !cust.getAvatarUrl().isBlank()) {
                userAvatar = cust.getAvatarUrl();
            }
        }

        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getFullName())
                .email(user.getEmail())
                .role(roleName)
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchCode(user.getBranch() != null ? user.getBranch().getBranchCode() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getBranchName() : null)
                .avatar(userAvatar)
                .permissions(permissions)
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .user(userInfo)
                .build();
    }

    @Override
    public void sendForgotPasswordOtp(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy tài khoản nào liên kết với email " + email));

        // Sinh mã OTP 6 chữ số ngẫu nhiên
        String otp = String.format("%06d", new java.util.Random().nextInt(1000000));
        otpCache.put(email, new OtpEntry(otp, LocalDateTime.now().plusMinutes(10)));

        emailService.sendForgotPasswordOtpEmail(user.getEmail(), user.getFullName(), otp);
        log.info("[AuthService] Đã gửi mã OTP đặt lại mật khẩu đến email: [{}]", email);
    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        OtpEntry entry = otpCache.get(email);
        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã xác thực OTP không tồn tại hoặc đã hết hạn (hiệu lực 10 phút).");
        }
        if (!entry.otp().equals(request.getOtp().trim())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã xác thực OTP không chính xác.");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        OtpEntry entry = otpCache.get(email);
        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã xác thực OTP không tồn tại hoặc đã hết hạn.");
        }
        if (!entry.otp().equals(request.getOtp().trim())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã xác thực OTP không chính xác.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy tài khoản nào liên kết với email " + email));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Thu hồi toàn bộ refresh token cũ
        refreshTokenRepository.revokeAllByUser(user);

        // Xóa OTP khỏi cache sau khi đổi mật khẩu thành công
        otpCache.remove(email);
        log.info("[AuthService] Đặt lại mật khẩu thành công cho tài khoản: [{}]", user.getUsername());
    }
}
