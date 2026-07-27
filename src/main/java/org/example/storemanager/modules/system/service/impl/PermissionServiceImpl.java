package org.example.storemanager.modules.system.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.system.dto.response.permission.GroupedPermissionResponse;
import org.example.storemanager.modules.system.dto.response.permission.PermissionResponse;
import org.example.storemanager.modules.system.entity.Permission;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.system.repository.PermissionRepository;
import org.example.storemanager.modules.system.service.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        // Lưu ý: Đảm bảo bạn có hàm findAllPermissionsIncludeDeleted trong Repository
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
        List<Permission> allPermissions = permissionRepository.findAllByIsDeletedFalse();

        Map<String, List<PermissionResponse>> groupedMap = allPermissions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getModule));

        return groupedMap.entrySet().stream()
                .map(entry -> GroupedPermissionResponse.builder()
                        .module(entry.getKey())
                        .permissions(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by("module").ascending().and(Sort.by("id").ascending());
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
}