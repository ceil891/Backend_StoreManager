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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (permissionRepository.existsByPermissionCode(request.getPermissionCode())) {
            throw new DuplicateResourceException("Permission", "permissionCode", request.getPermissionCode());
        }

        Permission permission = Permission.builder()
                .permissionCode(request.getPermissionCode())
                .module(request.getModule())
                .description(request.getDescription())
                .build();

        permissionRepository.save(permission);

        return CreatePermissionResponse.builder()
                .id(permission.getId())
                .permissionCode(permission.getPermissionCode())
                .module(permission.getModule())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE", entityName = "Permission", entityClass = Permission.class)
    public UpdatePermissionResponse updatePermission(Long id, UpdatePermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        permission.setModule(request.getModule());
        permission.setDescription(request.getDescription());

        permissionRepository.save(permission);

        return UpdatePermissionResponse.builder()
                .id(permission.getId())
                .permissionCode(permission.getPermissionCode())
                .module(permission.getModule())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "DELETE", entityName = "Permission", entityClass = Permission.class)
    public DeletePermissionResponse deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        permission.setIsDeleted(true);
        permissionRepository.save(permission);

        return DeletePermissionResponse.builder()
                .id(permission.getId())
                .permissionCode(permission.getPermissionCode())
                .isDeleted(true)
                .build();
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
        return mapToResponse(permission);
    }

    @Override
    public PageResponse<PermissionResponse> getAllPermissions(Pageable pageable) {
        Page<Permission> permissionPage = permissionRepository.findAll(pageable);
        List<PermissionResponse> content = permissionPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<PermissionResponse>builder()
                .content(content)
                .page(permissionPage.getNumber())
                .size(permissionPage.getSize())
                .totalElements(permissionPage.getTotalElements())
                .totalPages(permissionPage.getTotalPages())
                .last(permissionPage.isLast())
                .build();
    }

    @Override
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

    private PermissionResponse mapToResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .permissionCode(permission.getPermissionCode())
                .module(permission.getModule())
                .description(permission.getDescription())
                .build();
    }
}