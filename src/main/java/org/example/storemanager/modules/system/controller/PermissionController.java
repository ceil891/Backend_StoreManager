package org.example.storemanager.modules.system.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.system.dto.response.permission.GroupedPermissionResponse;
import org.example.storemanager.modules.system.dto.response.permission.PermissionResponse;
import org.example.storemanager.modules.system.entity.Permission;
import org.example.storemanager.modules.system.repository.PermissionRepository;
import org.example.storemanager.modules.system.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionRepository permissionRepository;

    // ========== XEM CHI TIẾT THEO ID ==========
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {
        PermissionResponse response = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH (phân trang hoặc tất cả) ==========
    @GetMapping
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
    public ResponseEntity<ApiResponse<List<GroupedPermissionResponse>>> getGroupedPermissions() {
        List<GroupedPermissionResponse> response = permissionService.getGroupedPermissions();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== THÊM QUYỀN MỚI ==========
    @PostMapping
    public ResponseEntity<ApiResponse<Permission>> createPermission(@RequestBody Permission req) {
        if (req.getPermissionCode() == null || req.getPermissionCode().isBlank()) {
            throw new IllegalArgumentException("Mã quyền không được để trống");
        }
        if (req.getModule() == null || req.getModule().isBlank()) {
            req.setModule("Hệ thống");
        }
        if (req.getIsActive() == null) {
            req.setIsActive(true);
        }
        req.setIsDeleted(false);
        Permission saved = permissionRepository.save(req);
        return ResponseEntity.status(201).body(ApiResponse.created(saved));
    }

    // ========== CẬP NHẬT QUYỀN ==========
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Permission>> updatePermission(@PathVariable Long id, @RequestBody Permission req) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
        if (req.getPermissionCode() != null && !req.getPermissionCode().isBlank()) {
            p.setPermissionCode(req.getPermissionCode());
        }
        if (req.getModule() != null && !req.getModule().isBlank()) {
            p.setModule(req.getModule());
        }
        if (req.getDescription() != null) {
            p.setDescription(req.getDescription());
        }
        if (req.getIsActive() != null) {
            p.setIsActive(req.getIsActive());
        }
        Permission updated = permissionRepository.save(p);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật quyền thành công", updated));
    }

    // ========== XÓA QUYỀN (SOFT DELETE) ==========
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
        p.setIsDeleted(true);
        permissionRepository.save(p);
        return ResponseEntity.ok(ApiResponse.ok("Xóa quyền thành công", null));
    }
}