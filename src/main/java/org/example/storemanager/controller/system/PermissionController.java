package org.example.storemanager.controller.system;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.system.permission.CreatePermissionRequest;
import org.example.storemanager.dto.request.system.permission.UpdatePermissionRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.permission.CreatePermissionResponse;
import org.example.storemanager.dto.response.system.permission.UpdatePermissionResponse;
import org.example.storemanager.dto.response.system.permission.DeletePermissionResponse;
import org.example.storemanager.dto.response.system.permission.GroupedPermissionResponse;
import org.example.storemanager.dto.response.system.permission.PermissionResponse;
import org.example.storemanager.service.system.PermissionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:create')")
    public ApiResponse<CreatePermissionResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        return ApiResponse.created("Tạo quyền thành công", permissionService.createPermission(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:update')")
    public ApiResponse<UpdatePermissionResponse> updatePermission(@PathVariable Long id, @Valid @RequestBody UpdatePermissionRequest request) {
        return ApiResponse.ok("Cập nhật thông tin quyền thành công", permissionService.updatePermission(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:delete')")
    public ApiResponse<DeletePermissionResponse> deletePermission(@PathVariable Long id) {
        return ApiResponse.ok("Xóa quyền thành công", permissionService.deletePermission(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:view')")
    public ApiResponse<PermissionResponse> getPermissionById(@PathVariable Long id) {
        return ApiResponse.ok(permissionService.getPermissionById(id));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:view')")
    public ApiResponse<PageResponse<PermissionResponse>> getAllPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        return ApiResponse.ok(permissionService.getAllPermissions(pageable));
    }

    @GetMapping("/grouped")
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:view')")
    public ApiResponse<List<GroupedPermissionResponse>> getGroupedPermissions() {
        return ApiResponse.ok(permissionService.getGroupedPermissions());
    }
}