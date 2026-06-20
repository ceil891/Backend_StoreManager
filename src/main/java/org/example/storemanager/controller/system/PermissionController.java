package org.example.storemanager.controller.system;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.system.permission.CreatePermissionRequest;
import org.example.storemanager.dto.request.system.permission.UpdatePermissionRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.system.permission.CreatePermissionResponse;
import org.example.storemanager.dto.response.system.permission.UpdatePermissionResponse;
import org.example.storemanager.dto.response.system.permission.DeletePermissionResponse;
import org.example.storemanager.dto.response.system.permission.GroupedPermissionResponse;
import org.example.storemanager.dto.response.system.permission.PermissionResponse;
import org.example.storemanager.service.system.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // ========== TẠO MỚI ==========
    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:create')")
    public ResponseEntity<ApiResponse<CreatePermissionResponse>> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        CreatePermissionResponse response = permissionService.createPermission(request);
        return ResponseEntity.status(201).body(ApiResponse.created("Tạo quyền thành công", response));
    }

    // ========== CẬP NHẬT ==========
    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:update')")
    public ResponseEntity<ApiResponse<UpdatePermissionResponse>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        UpdatePermissionResponse response = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin quyền thành công", response));
    }

    // ========== CẬP NHẬT TRẠNG THÁI ==========
    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:update-status')")
    public ResponseEntity<ApiResponse<UpdatePermissionResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        UpdatePermissionResponse response = permissionService.updateStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái quyền thành công", response));
    }

    // ========== XÓA MỀM ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:delete')")
    public ResponseEntity<ApiResponse<DeletePermissionResponse>> deletePermission(@PathVariable Long id) {
        DeletePermissionResponse response = permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa quyền thành công", response));
    }

    // ========== XEM CHI TIẾT THEO ID ==========
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:view')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {
        PermissionResponse response = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH (phân trang hoặc tất cả) ==========
    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:view')")
    public ResponseEntity<ApiResponse<?>> getPermissions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    permissionService.getPermissionsPaginated(search, isActive, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                    permissionService.getAllPermissions(search, isActive, sort, includeDeleted)));
        }
    }

    // ========== GROUP PERMISSIONS (Dành cho việc Load cây phân quyền ở Client) ==========
    @GetMapping("/grouped")
    @PreAuthorize("@securityEvaluator.hasPermission('system:permission:view')")
    public ResponseEntity<ApiResponse<List<GroupedPermissionResponse>>> getGroupedPermissions() {
        List<GroupedPermissionResponse> response = permissionService.getGroupedPermissions();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}