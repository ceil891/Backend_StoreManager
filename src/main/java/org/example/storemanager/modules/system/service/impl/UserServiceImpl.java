package org.example.storemanager.modules.system.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.shared.config.LogActivity;
import org.example.storemanager.modules.system.dto.request.user.CreateUserRequest;
import org.example.storemanager.modules.system.dto.request.user.ResetPasswordRequest;
import org.example.storemanager.modules.system.dto.request.user.UpdateUserRequest;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.system.dto.response.user.CreateUserResponse;
import org.example.storemanager.modules.system.dto.response.user.DeleteUserResponse;
import org.example.storemanager.modules.system.dto.response.user.UpdateUserResponse;
import org.example.storemanager.modules.system.dto.response.user.UserResponse;
import org.example.storemanager.modules.system.entity.Role;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.shared.enums.ErrorCode;
import org.example.storemanager.shared.exception.BusinessException;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.system.repository.RoleRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.example.storemanager.modules.system.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.partnerarea.entity.Customer;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.example.storemanager.shared.service.EmailService emailService;
    private final org.example.storemanager.modules.partnerarea.repository.CustomerRepository customerRepository;
    private final org.example.storemanager.modules.system.repository.RefreshTokenRepository refreshTokenRepository;


    @Override
    @Transactional
    @LogActivity(actionType = "CREATE", entityName = "User", entityClass = User.class)
    public CreateUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank() && userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank()) 
                ? request.getPassword() 
                : generateRandomPassword();

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(role);
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId()).orElse(null);
            user.setBranch(branch);
        }
        user.setTaxId(request.getTaxId());
        user.setIdentityId(request.getIdentityId());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setDepartmentId(request.getDepartmentId());
        user.setPositionId(request.getPositionId());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE");

        user.setIsDeleted(false);
        user.setCreatedBy(getCurrentUsername());

        User savedUser = userRepository.save(user);

        // Gửi email thông tin tài khoản nếu email không trống
        if (savedUser.getEmail() != null && !savedUser.getEmail().isBlank()) {
            emailService.sendAccountInfoEmail(savedUser.getEmail(), savedUser.getFullName(), savedUser.getUsername(), rawPassword);
        }

        // Gửi email thông báo cho Quản lý nếu có chỉ định managerId
        if (request.getManagerId() != null) {
            userRepository.findByIdAndIsDeletedFalse(request.getManagerId()).ifPresent(manager -> {
                if (manager.getEmail() != null && !manager.getEmail().isBlank()) {
                    String branchName = savedUser.getBranch() != null ? savedUser.getBranch().getBranchName() : "Toàn hệ thống";
                    String roleName = savedUser.getRole() != null ? (savedUser.getRole().getDescription() != null ? savedUser.getRole().getDescription() : savedUser.getRole().getRoleName()) : "Nhân viên";
                    emailService.sendManagerNotificationEmail(manager.getEmail(), manager.getFullName(), savedUser.getFullName(), savedUser.getEmail(), roleName, branchName);
                }
            });
        }

        return mapToCreateResponse(savedUser);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE", entityName = "User", entityClass = User.class)
    public UpdateUserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getRoleId() != null && (user.getRole() == null || !user.getRole().getId().equals(request.getRoleId()))) {
            Role role = roleRepository.findByIdAndIsDeletedFalse(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
            user.setRole(role);
        }

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                    .or(() -> branchRepository.findById(request.getBranchId()))
                    .orElse(null);
            user.setBranch(branch);
        } else {
            user.setBranch(null);
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(request.getStatus().toUpperCase());
            if (!"ACTIVE".equalsIgnoreCase(request.getStatus())) {
                refreshTokenRepository.revokeAllByUser(user);
            }
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setTaxId(request.getTaxId());
        user.setIdentityId(request.getIdentityId());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setDepartmentId(request.getDepartmentId());
        user.setPositionId(request.getPositionId());
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        user.setUpdatedBy(getCurrentUsername());

        User updatedUser = userRepository.save(user);
        org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(updatedUser.getUsername());
        org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(updatedUser.getEmail());

        // Đồng bộ ảnh đại diện và thông tin với Customer nếu có
        try {
            Customer cust = null;
            if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
                cust = customerRepository.findByEmailAndIsDeletedFalse(updatedUser.getEmail()).orElse(null);
            }
            if (cust == null && updatedUser.getPhone() != null && !updatedUser.getPhone().isBlank()) {
                cust = customerRepository.findByPhoneAndIsDeletedFalse(updatedUser.getPhone().replace(" ", "")).orElse(null);
            }
            if (cust != null) {
                if (updatedUser.getFullName() != null) cust.setName(updatedUser.getFullName());
                if (updatedUser.getPhone() != null) cust.setPhone(updatedUser.getPhone());
                if (updatedUser.getEmail() != null) cust.setEmail(updatedUser.getEmail());
                if (updatedUser.getAvatar() != null && !updatedUser.getAvatar().isBlank()) cust.setAvatarUrl(updatedUser.getAvatar());
                if (updatedUser.getStatus() != null) {
                    cust.setIsActive("ACTIVE".equalsIgnoreCase(updatedUser.getStatus()));
                }
                customerRepository.save(cust);
            }
        } catch (Exception ignored) {}

        return mapToUpdateResponse(updatedUser);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "User", entityClass = User.class)
    public UpdateUserResponse updateStatus(Long id, String status) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!status.equalsIgnoreCase("ACTIVE") && !status.equalsIgnoreCase("SUSPENDED") && !status.equalsIgnoreCase("TERMINATED")) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Trạng thái không hợp lệ");
        }

        user.setStatus(status.toUpperCase());
        user.setUpdatedBy(getCurrentUsername());

        // Thu hồi toàn bộ Refresh Token nếu bị khóa / đình chỉ / hủy
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            refreshTokenRepository.revokeAllByUser(user);
        }

        User updatedUser = userRepository.save(user);
        org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(updatedUser.getUsername());
        org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(updatedUser.getEmail());

        // Đồng bộ trạng thái khóa tài khoản sang Customer
        try {
            boolean isActive = "ACTIVE".equalsIgnoreCase(status);
            Customer cust = null;
            if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
                cust = customerRepository.findByEmailAndIsDeletedFalse(updatedUser.getEmail()).orElse(null);
            }
            if (cust == null && updatedUser.getPhone() != null && !updatedUser.getPhone().isBlank()) {
                cust = customerRepository.findByPhoneAndIsDeletedFalse(updatedUser.getPhone().replace(" ", "")).orElse(null);
            }
            if (cust != null) {
                cust.setIsActive(isActive);
                customerRepository.save(cust);
            }
        } catch (Exception ignored) {}

        return mapToUpdateResponse(updatedUser);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "RESET_PASSWORD", entityName = "User", entityClass = User.class)
    public void resetPassword(Long id, ResetPasswordRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedBy(getCurrentUsername());
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUser(user);
        org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(user.getUsername());
        org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(user.getEmail());

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            emailService.sendPasswordResetNotificationEmail(
                    user.getEmail(),
                    user.getFullName(),
                    user.getUsername(),
                    request.getNewPassword()
            );
        }
    }

    @Override
    @Transactional
    @LogActivity(actionType = "DELETE", entityName = "User", entityClass = User.class)
    public DeleteUserResponse deleteUser(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa tài khoản '" + user.getUsername() + "' vì tài khoản này vẫn đang HOẠT ĐỘNG. " +
                            "Vui lòng chuyển trạng thái thành TERMINATED hoặc SUSPENDED trước khi xóa."
            );
        }

        String username = getCurrentUsername();
        user.setIsDeleted(true);
        user.setStatus("TERMINATED");
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(username);
        user.setUpdatedBy(username);

        refreshTokenRepository.revokeAllByUser(user);
        User deletedUser = userRepository.save(user);
        org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(deletedUser.getUsername());
        org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(deletedUser.getEmail());

        return DeleteUserResponse.builder()
                .id(deletedUser.getId())
                .username(deletedUser.getUsername())
                .isDeleted(deletedUser.getIsDeleted())
                .deletedBy(deletedUser.getDeletedBy())
                .deletedAt(deletedUser.getDeletedAt() != null ? deletedUser.getDeletedAt() : LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(String search, String status, Long roleId, Long branchId, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, 1000, sorting);
        boolean hasFilter = (search != null && !search.trim().isEmpty())
                || (status != null && !status.trim().isEmpty())
                || roleId != null
                || branchId != null
                || includeDeleted;

        Page<User> pageResult;
        if (!hasFilter) {
            pageResult = userRepository.findByIsDeletedFalse(pageable);
        } else {
            pageResult = userRepository.findAllUsersIncludeDeleted(
                    (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                    (status != null && !status.trim().isEmpty()) ? status.trim() : null,
                    roleId,
                    branchId,
                    includeDeleted,
                    pageable);
        }

        java.util.Set<String> customerRoles = java.util.Set.of("CUSTOMER", "KHÁCH HÀNG", "KHACH HANG", "USER", "NGƯỜI DÙNG", "NGUOI DUNG");
        return pageResult.getContent().stream()
                .filter(u -> {
                    if (roleId != null) return true;
                    if (u.getRole() == null) return false;
                    String rName = u.getRole().getRoleName() != null ? u.getRole().getRoleName().trim().toUpperCase() : "";
                    return !customerRoles.contains(rName);
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsersPaginated(String search, String status, Long roleId, Long branchId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        boolean hasFilter = (search != null && !search.trim().isEmpty())
                || (status != null && !status.trim().isEmpty())
                || roleId != null
                || branchId != null
                || includeDeleted;

        Page<User> pageResult;
        if (!hasFilter) {
            pageResult = userRepository.findByIsDeletedFalse(pageable);
        } else {
            pageResult = userRepository.findAllUsersIncludeDeleted(
                    (search != null && !search.trim().isEmpty()) ? search.trim() : null,
                    (status != null && !status.trim().isEmpty()) ? status.trim() : null,
                    roleId,
                    branchId,
                    includeDeleted,
                    pageable);
        }

        java.util.Set<String> customerRoles = java.util.Set.of("CUSTOMER", "KHÁCH HÀNG", "KHACH HANG", "USER", "NGƯỜI DÙNG", "NGUOI DUNG");
        List<UserResponse> content = pageResult.getContent().stream()
                .filter(u -> {
                    if (roleId != null) return true;
                    if (u.getRole() == null) return false;
                    String rName = u.getRole().getRoleName() != null ? u.getRole().getRoleName().trim().toUpperCase() : "";
                    return !customerRoles.contains(rName);
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by("id").descending();
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "RESTORE", entityName = "User", entityClass = User.class)
    public UserResponse restoreUser(Long id) {
        // Tìm kiếm thực thể bao gồm cả bản ghi đã bị xóa mềm
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (Boolean.FALSE.equals(user.getIsDeleted())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Tài khoản này hiện không nằm trong trạng thái đã xóa");
        }

        // Trả trạng thái tài khoản về ACTIVE
        user.setIsDeleted(false);
        user.setStatus("ACTIVE");
        user.setDeletedAt(null);
        user.setDeletedBy(null);
        user.setUpdatedBy(getCurrentUsername());
        user.setUpdatedAt(LocalDateTime.now());

        User restoredUser = userRepository.save(user);
        return mapToResponse(restoredUser);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getBranchName() : null)
                .taxId(user.getTaxId())
                .identityId(user.getIdentityId())
                .dateOfBirth(user.getDateOfBirth())
                .departmentId(user.getDepartmentId())
                .positionId(user.getPositionId())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .createdBy(user.getCreatedBy())
                .updatedAt(user.getUpdatedAt())
                .updatedBy(user.getUpdatedBy())
                .isDeleted(user.getIsDeleted())
                .build();
    }

    private CreateUserResponse mapToCreateResponse(User user) {
        return CreateUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .avatar(user.getAvatar())
                .createdBy(user.getCreatedBy())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    private UpdateUserResponse mapToUpdateResponse(User user) {
        return UpdateUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getBranchName() : null)
                .avatar(user.getAvatar())
                .updatedBy(user.getUpdatedBy())
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt() : LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE_ROLE_BRANCH", entityName = "User", entityClass = User.class)
    public UpdateUserResponse updateRoleAndBranch(Long id, Long roleId, Long branchId) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (roleId != null) {
            Role role = roleRepository.findByIdAndIsDeletedFalse(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
            user.setRole(role);
        }

        if (branchId != null) {
            Branch branch = branchRepository.findByIdAndIsDeletedFalse(branchId)
                    .or(() -> branchRepository.findById(branchId))
                    .orElse(null);
            user.setBranch(branch);
        } else {
            user.setBranch(null);
        }

        user.setUpdatedBy(getCurrentUsername());
        User updatedUser = userRepository.save(user);
        return mapToUpdateResponse(updatedUser);
    }
}