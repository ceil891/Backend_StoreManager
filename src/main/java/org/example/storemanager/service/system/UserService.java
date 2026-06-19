package org.example.storemanager.service.system;

import org.example.storemanager.dto.request.system.user.CreateUserRequest;
import org.example.storemanager.dto.request.system.user.ResetPasswordRequest;
import org.example.storemanager.dto.request.system.user.UpdateUserRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.user.CreateUserResponse;
import org.example.storemanager.dto.response.system.user.DeleteUserResponse;
import org.example.storemanager.dto.response.system.user.UpdateUserResponse;
import org.example.storemanager.dto.response.system.user.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    CreateUserResponse createUser(CreateUserRequest request);
    UpdateUserResponse updateUser(Long id, UpdateUserRequest request);
    UpdateUserResponse updateStatus(Long id, String status);
    DeleteUserResponse deleteUser(Long id);
    UserResponse getUserById(Long id);
    PageResponse<UserResponse> getAllUsers(String search, String status, Long roleId, Long branchId, Pageable pageable);
    void resetPassword(Long id, ResetPasswordRequest request);
}