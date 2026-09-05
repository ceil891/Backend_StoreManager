package org.example.storemanager.modules.system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.system.dto.request.user.CreateUserRequest;
import org.example.storemanager.modules.system.dto.request.user.ResetPasswordRequest;
import org.example.storemanager.modules.system.dto.request.user.UpdateUserRequest;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.system.dto.response.user.CreateUserResponse;
import org.example.storemanager.modules.system.dto.response.user.DeleteUserResponse;
import org.example.storemanager.modules.system.dto.response.user.UpdateUserResponse;
import org.example.storemanager.modules.system.dto.response.user.UserResponse;
import org.example.storemanager.modules.system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.example.storemanager.modules.system.dto.request.user.UpdateUserRoleBranchRequest;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ========== TẠO MỚI ==========
    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:create')")
    public ResponseEntity<ApiResponse<CreateUserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserResponse response = userService.createUser(request);
        return ResponseEntity.status(201).body(ApiResponse.created("Tạo người dùng thành công", response));
    }

    // ========== CẬP NHẬT ==========
    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:update')")
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UpdateUserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin người dùng thành công", response));
    }

    // ========== CẬP NHẬT VAI TRÒ & CHI NHÁNH (DEDICATED API) ==========
    @PutMapping("/{id}/role-branch")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:update')")
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateRoleAndBranch(
            @PathVariable Long id,
            @RequestBody UpdateUserRoleBranchRequest request) {
        Long rId = request != null ? request.getRoleId() : null;
        Long bId = request != null ? request.getBranchId() : null;
        UpdateUserResponse response = userService.updateRoleAndBranch(id, rId, bId);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật vai trò và chi nhánh người dùng thành công", response));
    }


    // ========== CẬP NHẬT TRẠNG THÁI ==========

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:update-status')")
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        UpdateUserResponse response = userService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", response));
    }

    // ========== RESET PASSWORD ==========
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:reset-password') or @securityEvaluator.hasPermission('system:user:update')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Khôi phục mật khẩu thành công", null));
    }

    // ========== XÓA MỀM ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:delete')")
    public ResponseEntity<ApiResponse<DeleteUserResponse>> deleteUser(@PathVariable Long id) {
        DeleteUserResponse response = userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa người dùng thành công", response));
    }

    // ========== XEM CHI TIẾT THEO ID ==========
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:view')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH (phân trang hoặc tất cả) ==========
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        log.info(">>> [UserController] Received GET /api/v1/users (search={}, status={}, roleId={}, branchId={})", search, status, roleId, branchId);
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    userService.getUsersPaginated(search, status, roleId, branchId, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                    userService.getAllUsers(search, status, roleId, branchId, sort, includeDeleted)));
        }
    }

    // ========== KHÔI PHỤC (RESTORE) ==========
    @PutMapping("/{id}/restore")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:restore')")
    public ResponseEntity<ApiResponse<UserResponse>> restoreUser(@PathVariable Long id) {
        UserResponse response = userService.restoreUser(id);
        return ResponseEntity.ok(ApiResponse.ok("Khôi phục tài khoản người dùng thành công", response));
    }
}