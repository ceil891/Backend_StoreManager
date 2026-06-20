package org.example.storemanager.service.system;

import org.example.storemanager.dto.request.system.permission.CreatePermissionRequest;
import org.example.storemanager.dto.request.system.permission.UpdatePermissionRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.permission.CreatePermissionResponse;
import org.example.storemanager.dto.response.system.permission.UpdatePermissionResponse;
import org.example.storemanager.dto.response.system.permission.DeletePermissionResponse;
import org.example.storemanager.dto.response.system.permission.GroupedPermissionResponse;
import org.example.storemanager.dto.response.system.permission.PermissionResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PermissionService {
    CreatePermissionResponse createPermission(CreatePermissionRequest request);
    UpdatePermissionResponse updatePermission(Long id, UpdatePermissionRequest request);
    DeletePermissionResponse deletePermission(Long id);
    PermissionResponse getPermissionById(Long id);
    PageResponse<PermissionResponse> getAllPermissions(Pageable pageable);
    List<GroupedPermissionResponse> getGroupedPermissions();
}