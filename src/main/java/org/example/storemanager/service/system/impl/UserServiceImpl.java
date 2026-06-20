package org.example.storemanager.service.system.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.system.user.CreateUserRequest;
import org.example.storemanager.dto.request.system.user.ResetPasswordRequest;
import org.example.storemanager.dto.request.system.user.UpdateUserRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.user.CreateUserResponse;
import org.example.storemanager.dto.response.system.user.DeleteUserResponse;
import org.example.storemanager.dto.response.system.user.UpdateUserResponse;
import org.example.storemanager.dto.response.system.user.UserResponse;
import org.example.storemanager.entity.system.Role;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.enums.ErrorCode;
import org.example.storemanager.exception.BusinessException;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.system.RoleRepository;
import org.example.storemanager.repository.system.UserRepository;
import org.example.storemanager.service.system.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @LogActivity(actionType = "CREATE", entityName = "User", entityClass = User.class)
    public CreateUserResponse createUser(CreateUserRequest request) {
        // Check duplicate kết hợp điều kiện chưa bị xóa mềm
        if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE");

        // Auditing
        user.setIsDeleted(false);
        user.setCreatedBy(getCurrentUsername());

        userRepository.save(user);

        return CreateUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE", entityName = "User", entityClass = User.class)
    public UpdateUserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!user.getRole().getId().equals(request.getRoleId())) {
            Role role = roleRepository.findByIdAndIsDeletedFalse(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
            user.setRole(role);
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // Auditing
        user.setUpdatedBy(getCurrentUsername());

        userRepository.save(user);

        return UpdateUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .build();
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

        userRepository.save(user);

        return UpdateUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .build();
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
    }

    @Override
    @Transactional
    @LogActivity(actionType = "DELETE", entityName = "User", entityClass = User.class)
    public DeleteUserResponse deleteUser(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Không thể xóa tài khoản đang hoạt động. Vui lòng ngưng hoạt động trước khi xóa.");
        }

        String username = getCurrentUsername();
        user.setIsDeleted(true);
        user.setStatus("TERMINATED"); // Vô hiệu hóa triệt để khi xóa mềm
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(username);
        user.setUpdatedBy(username);

        userRepository.save(user);

        return DeleteUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .isDeleted(true)
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
    public PageResponse<UserResponse> getAllUsers(String search, String status, Long roleId, Long branchId, Pageable pageable) {
        // NOTE: Bạn cần định nghĩa hàm này trong UserRepository (dùng @Query hoặc Specification)
        Page<User> userPage = userRepository.findAllUsersIncludeDeleted(search, status, roleId, branchId, false, pageable);

        List<UserResponse> content = userPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .content(content)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
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
                .createdAt(user.getCreatedAt())
                .isDeleted(user.getIsDeleted())
                .build();
    }
}