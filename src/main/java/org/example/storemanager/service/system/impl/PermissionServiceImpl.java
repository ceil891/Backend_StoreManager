package org.example.storemanager.service.system.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.system.permission.CreatePermissionRequest;
import org.example.storemanager.dto.request.system.permission.UpdatePermissionRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.permission.CreatePermissionResponse;
import org.example.storemanager.dto.response.system.permission.UpdatePermissionResponse;
import org.example.storemanager.dto.response.system.permission.DeletePermissionResponse;
import org.example.storemanager.dto.response.system.permission.GroupedPermissionResponse;
import org.example.storemanager.dto.response.system.permission.PermissionResponse;
import org.example.storemanager.entity.system.Permission;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.system.PermissionRepository;
import org.example.storemanager.service.system.PermissionService;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    @LogActivity(actionType = "CREATE", entityName = "Permission", entityClass = Permission.class)
    public CreatePermissionResponse createPermission(CreatePermissionRequest request) {
        if (permissionRepository.existsByPermissionCodeAndIsDeletedFalse(request.getPermissionCode())) {
            throw new DuplicateResourceException("Permission", "permissionCode", request.getPermissionCode());
        }

        Permission permission = Permission.builder()
                .permissionCode(request.getPermissionCode())
                .module(request.getModule())
                .description(request.getDescription())
                .isActive(true)
                .build();

        permission.setIsDeleted(false);
        permission.setCreatedBy(getCurrentUsername());

        Permission savedPermission = permissionRepository.save(permission);
        return mapToCreateResponse(savedPermission);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE", entityName = "Permission", entityClass = Permission.class)
    public UpdatePermissionResponse updatePermission(Long id, UpdatePermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        permission.setModule(request.getModule());
        permission.setDescription(request.getDescription());
        permission.setUpdatedBy(getCurrentUsername());

        Permission updatedPermission = permissionRepository.save(permission);
        return mapToUpdateResponse(updatedPermission);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "Permission", entityClass = Permission.class)
    public UpdatePermissionResponse updateStatus(Long id, Boolean isActive) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        permission.setIsActive(isActive);
        permission.setUpdatedBy(getCurrentUsername());

        Permission updatedPermission = permissionRepository.save(permission);
        return mapToUpdateResponse(updatedPermission);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "DELETE", entityName = "Permission", entityClass = Permission.class)
    public DeletePermissionResponse deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        if (Boolean.TRUE.equals(permission.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa quyền '" + permission.getPermissionCode() + "' vì quyền này vẫn đang HOẠT ĐỘNG. " +
                            "Vui lòng tắt hoạt động trước, sau đó mới có thể xóa."
            );
        }

        String username = getCurrentUsername();
        permission.setIsDeleted(true);
        permission.setIsActive(false);
        permission.setDeletedAt(LocalDateTime.now());
        permission.setDeletedBy(username);
        permission.setUpdatedBy(username);

        Permission deletedPermission = permissionRepository.save(permission);

        return DeletePermissionResponse.builder()
                .id(deletedPermission.getId())
                .permissionCode(deletedPermission.getPermissionCode())
                .isDeleted(deletedPermission.getIsDeleted())
                .deletedBy(deletedPermission.getDeletedBy())
                .deletedAt(deletedPermission.getDeletedAt() != null ? deletedPermission.getDeletedAt() : LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
        return mapToResponse(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions(String search, Boolean isActive, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        // Lưu ý: Cần khai báo findAllPermissionsIncludeDeleted trong PermissionRepository
        Page<Permission> pageResult = permissionRepository.findAllPermissionsIncludeDeleted(search, isActive, includeDeleted, pageable);

        return pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionResponse> getPermissionsPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Permission> pageResult = permissionRepository.findAllPermissionsIncludeDeleted(search, isActive, includeDeleted, pageable);

        List<PermissionResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<PermissionResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupedPermissionResponse> getGroupedPermissions() {
        List<Permission> permissions = permissionRepository.findByIsDeletedFalse();

        Map<String, List<PermissionResponse>> grouped = permissions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getModule));

        return grouped.entrySet().stream()
                .map(entry -> GroupedPermissionResponse.builder()
                        .module(entry.getKey())
                        .permissions(entry.getValue())
                        .build())
                .collect(Collectors.toList());
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

    private PermissionResponse mapToResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .permissionCode(permission.getPermissionCode())
                .module(permission.getModule())
                .description(permission.getDescription())
                .isActive(permission.getIsActive())
                .createdAt(permission.getCreatedAt())
                .createdBy(permission.getCreatedBy())
                .updatedAt(permission.getUpdatedAt())
                .updatedBy(permission.getUpdatedBy())
                .isDeleted(permission.getIsDeleted())
                .build();
    }

    private CreatePermissionResponse mapToCreateResponse(Permission permission) {
        return CreatePermissionResponse.builder()
                .id(permission.getId())
                .permissionCode(permission.getPermissionCode())
                .module(permission.getModule())
                .createdBy(permission.getCreatedBy())
                .createdAt(permission.getCreatedAt() != null ? permission.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    private UpdatePermissionResponse mapToUpdateResponse(Permission permission) {
        return UpdatePermissionResponse.builder()
                .id(permission.getId())
                .permissionCode(permission.getPermissionCode())
                .module(permission.getModule())
                .isActive(permission.getIsActive())
                .updatedBy(permission.getUpdatedBy())
                .updatedAt(permission.getUpdatedAt() != null ? permission.getUpdatedAt() : LocalDateTime.now())
                .build();
    }
}