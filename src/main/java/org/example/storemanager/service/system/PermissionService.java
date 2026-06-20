package org.example.storemanager.service.system;

import org.example.storemanager.dto.request.system.permission.CreatePermissionRequest;
import org.example.storemanager.dto.request.system.permission.UpdatePermissionRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.permission.CreatePermissionResponse;
import org.example.storemanager.dto.response.system.permission.DeletePermissionResponse;
import org.example.storemanager.dto.response.system.permission.GroupedPermissionResponse;
import org.example.storemanager.dto.response.system.permission.PermissionResponse;
import org.example.storemanager.dto.response.system.permission.UpdatePermissionResponse;

import java.util.List;

public interface PermissionService {

    CreatePermissionResponse createPermission(CreatePermissionRequest request);

    UpdatePermissionResponse updatePermission(Long id, UpdatePermissionRequest request);

    DeletePermissionResponse deletePermission(Long id);

    UpdatePermissionResponse updateStatus(Long id, Boolean isActive);

    PermissionResponse getPermissionById(Long id);

    List<PermissionResponse> getAllPermissions(String search, Boolean isActive, String sort, boolean includeDeleted);

    PageResponse<PermissionResponse> getPermissionsPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted);

    List<GroupedPermissionResponse> getGroupedPermissions();
}