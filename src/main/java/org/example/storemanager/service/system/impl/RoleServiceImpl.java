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
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.system.PermissionRepository;
import org.example.storemanager.repository.system.RolePermissionRepository;
import org.example.storemanager.repository.system.RoleRepository;
import org.example.storemanager.service.system.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        if (roleRepository.existsByRoleNameAndIsDeletedFalse(request.getRoleName())) {
            throw new DuplicateResourceException("Role", "roleName", request.getRoleName());
        }

        Role role = Role.builder()
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        // 1. Set giá trị Audit thủ công
        role.setIsDeleted(false);
        String username = getCurrentUsername();
        role.setCreatedBy(username);
        role.setCreatedAt(LocalDateTime.now()); // Đảm bảo có giá trị ngay

        // 2. Lưu và hứng lại đối tượng đã persist (nếu cần lấy ID/Time từ DB)
        Role savedRole = roleRepository.save(role);

        // 3. Map chính xác đối tượng đã lưu
        return mapToCreateResponse(savedRole);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE", entityName = "Role", entityClass = Role.class)
    public UpdateRoleResponse updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (roleRepository.existsByRoleNameAndIdNotAndIsDeletedFalse(request.getRoleName(), id)) {
            throw new DuplicateResourceException("Role", "roleName", request.getRoleName());
        }

        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setUpdatedBy(getCurrentUsername());

        Role updatedRole = roleRepository.save(role);
        return mapToUpdateResponse(updatedRole);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "Role", entityClass = Role.class)
    public UpdateRoleResponse updateStatus(Long id, Boolean isActive) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        role.setIsActive(isActive);
        role.setUpdatedBy(getCurrentUsername());

        Role updatedRole = roleRepository.save(role);
        return mapToUpdateResponse(updatedRole);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "DELETE", entityName = "Role", entityClass = Role.class)
    public DeleteRoleResponse deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (Boolean.TRUE.equals(role.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa vai trò '" + role.getRoleName() + "' vì vai trò này vẫn đang HOẠT ĐỘNG. " +
                            "Vui lòng tắt hoạt động trước, sau đó mới có thể xóa."
            );
        }

        String username = getCurrentUsername();
        role.setIsDeleted(true);
        role.setIsActive(false);
        role.setDeletedAt(LocalDateTime.now());
        role.setDeletedBy(username);
        role.setUpdatedBy(username);

        Role deletedRole = roleRepository.save(role);

        return DeleteRoleResponse.builder()
                .id(deletedRole.getId())
                .roleName(deletedRole.getRoleName())
                .isDeleted(deletedRole.getIsDeleted())
                .deletedBy(deletedRole.getDeletedBy())
                .deletedAt(deletedRole.getDeletedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return mapToResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles(String search, Boolean isActive, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        // Lưu ý: Bạn cần khai báo hàm findAllRolesIncludeDeleted trong RoleRepository
        Page<Role> pageResult = roleRepository.findAllRolesIncludeDeleted(search, isActive, includeDeleted, pageable);

        return pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> getRolesPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Role> pageResult = roleRepository.findAllRolesIncludeDeleted(search, isActive, includeDeleted, pageable);

        List<RoleResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<RoleResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "ASSIGN_PERMISSIONS", entityName = "Role", entityClass = Role.class)
    public void assignPermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        // 1. Chặn phân quyền nếu đã bị xóa mềm
        if (Boolean.TRUE.equals(role.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể phân quyền cho vai trò đã bị xóa.");
        }

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

        role.setUpdatedBy(getCurrentUsername());
        roleRepository.save(role);
    }

    // 2. API mới: Xóa phân quyền cụ thể
    @Override
    @Transactional
    @LogActivity(actionType = "REMOVE_PERMISSIONS", entityName = "Role", entityClass = Role.class)
    public void removePermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        if (Boolean.TRUE.equals(role.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể sửa phân quyền cho vai trò đã bị xóa.");
        }

        // Xóa những quyền có trong danh sách request truyền vào
        rolePermissionRepository.deleteByRoleIdAndPermissionIdIn(roleId, request.getPermissionIds());

        role.setUpdatedBy(getCurrentUsername());
        roleRepository.save(role);
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

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .isActive(role.getIsActive())
                .createdAt(role.getCreatedAt())
                .createdBy(role.getCreatedBy())
                .updatedAt(role.getUpdatedAt())
                .updatedBy(role.getUpdatedBy())
                .isDeleted(role.getIsDeleted())
                .build();
    }

    private CreateRoleResponse mapToCreateResponse(Role role) {
        return CreateRoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .createdBy(role.getCreatedBy())
                .createdAt(role.getCreatedAt() != null ? role.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    private UpdateRoleResponse mapToUpdateResponse(Role role) {
        return UpdateRoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .isActive(role.getIsActive())
                .updatedBy(role.getUpdatedBy())
                .updatedAt(role.getUpdatedAt() != null ? role.getUpdatedAt() : LocalDateTime.now())
                .build();
    }
}