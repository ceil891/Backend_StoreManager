package org.example.storemanager.service.system;

import org.example.storemanager.dto.request.system.role.AssignPermissionsRequest;
import org.example.storemanager.dto.request.system.role.CreateRoleRequest;
import org.example.storemanager.dto.request.system.role.UpdateRoleRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.role.*;

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