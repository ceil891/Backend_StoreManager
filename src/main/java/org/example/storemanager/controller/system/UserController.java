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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:create')")
    public ApiResponse<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        // Dùng hàm created(message, data) cho method POST
        return ApiResponse.created("Tạo người dùng thành công", userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:update')")
    public ApiResponse<UpdateUserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        // Dùng hàm ok(message, data)
        return ApiResponse.ok("Cập nhật thông tin người dùng thành công", userService.updateUser(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:update-status')")
    public ApiResponse<UpdateUserResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.ok("Cập nhật trạng thái thành công", userService.updateStatus(id, status));
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:reset-password')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        // Dùng hàm ok(message) khi chỉ trả về thông báo
        return ApiResponse.ok("Khôi phục mật khẩu thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:delete')")
    public ApiResponse<DeleteUserResponse> deleteUser(@PathVariable Long id) {
        return ApiResponse.ok("Xóa người dùng thành công", userService.deleteUser(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:view')")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        // Dùng hàm ok(data) khi không cần kèm message
        return ApiResponse.ok(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:user:view')")
    public ApiResponse<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        return ApiResponse.ok(userService.getAllUsers(search, status, roleId, branchId, pageable));
    }
}