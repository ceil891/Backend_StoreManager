package org.example.storemanager.service.system;

import org.example.storemanager.dto.request.system.user.CreateUserRequest;
import org.example.storemanager.dto.request.system.user.ResetPasswordRequest;
import org.example.storemanager.dto.request.system.user.UpdateUserRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.user.CreateUserResponse;
import org.example.storemanager.dto.response.system.user.DeleteUserResponse;
import org.example.storemanager.dto.response.system.user.UpdateUserResponse;
import org.example.storemanager.dto.response.system.user.UserResponse;

import java.util.List;

public interface UserService {

    CreateUserResponse createUser(CreateUserRequest request);

    UpdateUserResponse updateUser(Long id, UpdateUserRequest request);

    DeleteUserResponse deleteUser(Long id);

    UpdateUserResponse updateStatus(Long id, String status);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers(String search, String status, Long roleId, Long branchId, String sort, boolean includeDeleted);

    PageResponse<UserResponse> getUsersPaginated(String search, String status, Long roleId, Long branchId, int page, int size, String sort, boolean includeDeleted);

    void resetPassword(Long id, ResetPasswordRequest request);
}