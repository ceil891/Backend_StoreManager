package org.example.storemanager.controller.system;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.system.user.CreateUserRequest;
import org.example.storemanager.dto.request.system.user.ResetPasswordRequest;
import org.example.storemanager.dto.request.system.user.UpdateUserRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.user.CreateUserResponse;
import org.example.storemanager.dto.response.system.user.DeleteUserResponse;
import org.example.storemanager.dto.response.system.user.UpdateUserResponse;
import org.example.storemanager.dto.response.system.user.UserResponse;
import org.example.storemanager.service.system.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:reset-password')")
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
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:view')")
    public ResponseEntity<ApiResponse<?>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    userService.getUsersPaginated(search, status, roleId, branchId, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                    userService.getAllUsers(search, status, roleId, branchId, sort, includeDeleted)));
        }
    }
}