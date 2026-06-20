package org.example.storemanager.service.system;

import org.example.storemanager.dto.request.system.role.AssignPermissionsRequest;
import org.example.storemanager.dto.request.system.role.CreateRoleRequest;
import org.example.storemanager.dto.request.system.role.UpdateRoleRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.role.CreateRoleResponse;
import org.example.storemanager.dto.response.system.role.UpdateRoleResponse;
import org.example.storemanager.dto.response.system.role.DeleteRoleResponse;
import org.example.storemanager.dto.response.system.role.RoleResponse;
import org.springframework.data.domain.Pageable;

public interface RoleService {
    CreateRoleResponse createRole(CreateRoleRequest request);
    UpdateRoleResponse updateRole(Long id, UpdateRoleRequest request);
    UpdateRoleResponse updateStatus(Long id, Boolean isActive);
    DeleteRoleResponse deleteRole(Long id);
    RoleResponse getRoleById(Long id);
    PageResponse<RoleResponse> getAllRoles(Pageable pageable);
    void assignPermissions(Long roleId, AssignPermissionsRequest request);
}