package org.example.storemanager.controller.system;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.system.permission.GroupedPermissionResponse;
import org.example.storemanager.dto.response.system.permission.PermissionResponse;
import org.example.storemanager.service.system.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

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
            @RequestParam(defaultValue = "module,asc") String sort) {

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