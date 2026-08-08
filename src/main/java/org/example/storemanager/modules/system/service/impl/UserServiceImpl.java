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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    @LogActivity(actionType = "CREATE", entityName = "User", entityClass = User.class)
    public CreateUserResponse createUser(CreateUserRequest request) {
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

        user.setIsDeleted(false);
        user.setCreatedBy(getCurrentUsername());

        User savedUser = userRepository.save(user);
        return mapToCreateResponse(savedUser);
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
        user.setUpdatedBy(getCurrentUsername());

        User updatedUser = userRepository.save(user);
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

        User updatedUser = userRepository.save(user);
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

        User deletedUser = userRepository.save(user);

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
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<User> pageResult = userRepository.findAllUsersIncludeDeleted(search, status, roleId, branchId, includeDeleted, pageable);

        return pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsersPaginated(String search, String status, Long roleId, Long branchId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<User> pageResult = userRepository.findAllUsersIncludeDeleted(search, status, roleId, branchId, includeDeleted, pageable);

        List<UserResponse> content = pageResult.getContent().stream()
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
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));
            user.setBranch(branch);
        }

        user.setUpdatedBy(getCurrentUsername());
        User updatedUser = userRepository.save(user);
        return mapToUpdateResponse(updatedUser);
    }
}