package org.example.storemanager.service.system;

import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.permission.GroupedPermissionResponse;
import org.example.storemanager.dto.response.system.permission.PermissionResponse;

import java.util.List;

public interface PermissionService {

    PermissionResponse getPermissionById(Long id);

    List<PermissionResponse> getAllPermissions(String search, Boolean isActive, String sort, boolean includeDeleted);

    PageResponse<PermissionResponse> getPermissionsPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted);

    List<GroupedPermissionResponse> getGroupedPermissions();
}