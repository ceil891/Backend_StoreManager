package org.example.storemanager.service.system.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.system.role.AssignPermissionsRequest;
import org.example.storemanager.dto.request.system.role.CreateRoleRequest;
import org.example.storemanager.dto.request.system.role.UpdateRoleRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.role.CreateRoleResponse;
import org.example.storemanager.dto.response.system.role.UpdateRoleResponse;
import org.example.storemanager.dto.response.system.role.DeleteRoleResponse;
import org.example.storemanager.dto.response.system.role.RoleResponse;
import org.example.storemanager.entity.system.Permission;
import org.example.storemanager.entity.system.Role;
import org.example.storemanager.entity.system.RolePermission;
import org.example.storemanager.enums.ErrorCode;
import org.example.storemanager.exception.BusinessException;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.system.PermissionRepository;
import org.example.storemanager.repository.system.RolePermissionRepository;
import org.example.storemanager.repository.system.RoleRepository;
import org.example.storemanager.service.system.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    @LogActivity(actionType = "CREATE", entityName = "Role", entityClass = Role.class)
    public CreateRoleResponse createRole(CreateRoleRequest request) {
        // Đã sửa thành check có IsDeletedFalse
        if (roleRepository.existsByRoleNameAndIsDeletedFalse(request.getRoleName())) {
            throw new DuplicateResourceException("Role", "roleName", request.getRoleName());
        }

        Role role = Role.builder()
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        // Thêm Auditing
        role.setIsDeleted(false);
        role.setCreatedBy(getCurrentUsername());

        roleRepository.save(role);

        return CreateRoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE", entityName = "Role", entityClass = Role.class)
    public UpdateRoleResponse updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Thêm Check trùng lặp khi update
        if (roleRepository.existsByRoleNameAndIdNotAndIsDeletedFalse(request.getRoleName(), id)) {
            throw new DuplicateResourceException("Role", "roleName", request.getRoleName());
        }

        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setUpdatedBy(getCurrentUsername()); // Auditing

        roleRepository.save(role);

        return UpdateRoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .isActive(role.getIsActive())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "Role", entityClass = Role.class)
    public UpdateRoleResponse updateStatus(Long id, Boolean isActive) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        role.setIsActive(isActive);
        role.setUpdatedBy(getCurrentUsername()); // Auditing
        roleRepository.save(role);

        return UpdateRoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .isActive(role.getIsActive())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "DELETE", entityName = "Role", entityClass = Role.class)
    public DeleteRoleResponse deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Thêm chặn xóa nếu đang hoạt động
        if (Boolean.TRUE.equals(role.getIsActive())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Không thể xóa Role đang hoạt động. Vui lòng ngưng hoạt động trước.");
        }

        String username = getCurrentUsername();
        role.setIsDeleted(true);
        role.setIsActive(false); // Vô hiệu hóa
        role.setDeletedAt(LocalDateTime.now());
        role.setDeletedBy(username);
        role.setUpdatedBy(username);

        roleRepository.save(role);

        return DeleteRoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .isDeleted(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true) // Thêm Read Only
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .isActive(role.getIsActive())
                .createdAt(role.getCreatedAt())
                .isDeleted(role.getIsDeleted())
                .build();
    }

    @Override
    @Transactional(readOnly = true) // Thêm Read Only
    public PageResponse<RoleResponse> getAllRoles(Pageable pageable) {
        Page<Role> rolePage = roleRepository.findAll(pageable);
        List<RoleResponse> content = rolePage.getContent().stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .description(role.getDescription())
                        .isActive(role.getIsActive())
                        .createdAt(role.getCreatedAt())
                        .isDeleted(role.getIsDeleted())
                        .build())
                .collect(Collectors.toList());

        return PageResponse.<RoleResponse>builder()
                .content(content)
                .page(rolePage.getNumber())
                .size(rolePage.getSize())
                .totalElements(rolePage.getTotalElements())
                .totalPages(rolePage.getTotalPages())
                .last(rolePage.isLast())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "ASSIGN_PERMISSIONS", entityName = "Role", entityClass = Role.class)
    public void assignPermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        rolePermissionRepository.deleteByRoleId(roleId);

        List<RolePermission> rolePermissions = request.getPermissionIds().stream()
                .map(permissionId -> {
                    Permission permission = permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));
                    return RolePermission.builder()
                            .role(role)
                            .permission(permission)
                            .build();
                }).collect(Collectors.toList());

        rolePermissionRepository.saveAll(rolePermissions);
        role.setUpdatedBy(getCurrentUsername()); // Auditing
        roleRepository.save(role);
    }

    // Hàm lấy username phục vụ Auditing
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }
}