package org.example.storemanager.modules.system.service;

import org.example.storemanager.modules.system.dto.request.role.AssignPermissionsRequest;
import org.example.storemanager.modules.system.dto.request.role.CreateRoleRequest;
import org.example.storemanager.modules.system.dto.request.role.UpdateRoleRequest;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.system.dto.response.role.*;

import java.util.List;

public interface RoleService {

    CreateRoleResponse createRole(CreateRoleRequest request);

    UpdateRoleResponse updateRole(Long id, UpdateRoleRequest request);

    DeleteRoleResponse deleteRole(Long id);

    UpdateRoleResponse updateStatus(Long id, Boolean isActive);

    RoleResponse getRoleById(Long id);

    List<RoleResponse> getAllRoles(String search, Boolean isActive, String sort, boolean includeDeleted);

    PageResponse<RoleResponse> getRolesPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted);

    AssignPermissionsResponse assignPermissions(Long roleId, AssignPermissionsRequest request);

    RemovePermissionsResponse removePermissions(Long roleId, AssignPermissionsRequest request);

    RoleResponse restoreRole(Long id);


}