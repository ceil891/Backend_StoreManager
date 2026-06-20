package org.example.storemanager.service.system;

import org.example.storemanager.dto.request.system.role.AssignPermissionsRequest;
import org.example.storemanager.dto.request.system.role.CreateRoleRequest;
import org.example.storemanager.dto.request.system.role.UpdateRoleRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.role.CreateRoleResponse;
import org.example.storemanager.dto.response.system.role.DeleteRoleResponse;
import org.example.storemanager.dto.response.system.role.RoleResponse;
import org.example.storemanager.dto.response.system.role.UpdateRoleResponse;

import java.util.List;

public interface RoleService {

    CreateRoleResponse createRole(CreateRoleRequest request);

    UpdateRoleResponse updateRole(Long id, UpdateRoleRequest request);

    DeleteRoleResponse deleteRole(Long id);

    UpdateRoleResponse updateStatus(Long id, Boolean isActive);

    RoleResponse getRoleById(Long id);

    List<RoleResponse> getAllRoles(String search, Boolean isActive, String sort, boolean includeDeleted);

    PageResponse<RoleResponse> getRolesPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted);

    void assignPermissions(Long roleId, AssignPermissionsRequest request);

    void removePermissions(Long roleId, AssignPermissionsRequest request);
}