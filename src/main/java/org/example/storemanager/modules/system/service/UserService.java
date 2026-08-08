package org.example.storemanager.modules.system.service;

import org.example.storemanager.modules.system.dto.request.user.CreateUserRequest;
import org.example.storemanager.modules.system.dto.request.user.ResetPasswordRequest;
import org.example.storemanager.modules.system.dto.request.user.UpdateUserRequest;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.system.dto.response.user.CreateUserResponse;
import org.example.storemanager.modules.system.dto.response.user.DeleteUserResponse;
import org.example.storemanager.modules.system.dto.response.user.UpdateUserResponse;
import org.example.storemanager.modules.system.dto.response.user.UserResponse;

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

    UserResponse restoreUser(Long id);

    UpdateUserResponse updateRoleAndBranch(Long id, Long roleId, Long branchId);
}