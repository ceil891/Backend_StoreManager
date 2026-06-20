package org.example.storemanager.controller.system;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.system.role.AssignPermissionsRequest;
import org.example.storemanager.dto.request.system.role.CreateRoleRequest;
import org.example.storemanager.dto.request.system.role.UpdateRoleRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.role.CreateRoleResponse;
import org.example.storemanager.dto.response.system.role.UpdateRoleResponse;
import org.example.storemanager.dto.response.system.role.DeleteRoleResponse;
import org.example.storemanager.dto.response.system.role.RoleResponse;
import org.example.storemanager.service.system.RoleService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:create')")
    public ApiResponse<CreateRoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.created("Tạo vai trò thành công", roleService.createRole(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:update')")
    public ApiResponse<UpdateRoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponse.ok("Cập nhật thông tin vai trò thành công", roleService.updateRole(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:update-status')")
    public ApiResponse<UpdateRoleResponse> updateStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        return ApiResponse.ok("Cập nhật trạng thái vai trò thành công", roleService.updateStatus(id, isActive));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:delete')")
    public ApiResponse<DeleteRoleResponse> deleteRole(@PathVariable Long id) {
        return ApiResponse.ok("Xóa vai trò thành công", roleService.deleteRole(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:view')")
    public ApiResponse<RoleResponse> getRoleById(@PathVariable Long id) {
        return ApiResponse.ok(roleService.getRoleById(id));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:view')")
    public ApiResponse<PageResponse<RoleResponse>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        return ApiResponse.ok(roleService.getAllRoles(pageable));
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("@securityEvaluator.hasPermission('system:role:assign-permissions')")
    public ApiResponse<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody AssignPermissionsRequest request) {
        roleService.assignPermissions(id, request);
        return ApiResponse.ok("Phân quyền cho vai trò thành công");
    }
}