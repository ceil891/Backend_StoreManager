package org.example.storemanager.controller.system;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.system.role.AssignPermissionsRequest;
import org.example.storemanager.dto.request.system.role.CreateRoleRequest;
import org.example.storemanager.dto.request.system.role.UpdateRoleRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.system.role.*;
import org.example.storemanager.service.system.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // ========== TẠO MỚI ==========
    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:create')")
    public ResponseEntity<ApiResponse<CreateRoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        CreateRoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(201).body(ApiResponse.created("Tạo vai trò thành công", response));
    }

    // ========== CẬP NHẬT ==========
    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:update')")
    public ResponseEntity<ApiResponse<UpdateRoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        UpdateRoleResponse response = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin vai trò thành công", response));
    }

    // ========== CẬP NHẬT TRẠNG THÁI ==========
    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:update-status')")
    public ResponseEntity<ApiResponse<UpdateRoleResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        UpdateRoleResponse response = roleService.updateStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái vai trò thành công", response));
    }

    // ========== XÓA MỀM ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:delete')")
    public ResponseEntity<ApiResponse<DeleteRoleResponse>> deleteRole(@PathVariable Long id) {
        DeleteRoleResponse response = roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa vai trò thành công", response));
    }

    // ========== XEM CHI TIẾT THEO ID ==========
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:view')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse response = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH (phân trang hoặc tất cả) ==========
    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:view')")
    public ResponseEntity<ApiResponse<?>> getRoles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    roleService.getRolesPaginated(search, isActive, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                    roleService.getAllRoles(search, isActive, sort, includeDeleted)));
        }
    }

    // ========== GÁN QUYỀN (Riêng của Role) ==========
    @PostMapping("/{id}/permissions")
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:assign-permissions')")
    public ResponseEntity<ApiResponse<AssignPermissionsResponse>> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionsRequest request) {
        AssignPermissionsResponse response = roleService.assignPermissions(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Phân quyền cho vai trò thành công", response));
    }

    // ========== XÓA BỚT QUYỀN ==========
    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:assign-permissions')")
    public ResponseEntity<ApiResponse<RemovePermissionsResponse>> removePermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionsRequest request) {
        RemovePermissionsResponse response = roleService.removePermissions(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Xóa phân quyền khỏi vai trò thành công", response));
    }
}