package org.example.storemanager.modules.system.service;

import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.system.dto.response.permission.GroupedPermissionResponse;
import org.example.storemanager.modules.system.dto.response.permission.PermissionResponse;

import java.util.List;

public interface PermissionService {

    PermissionResponse getPermissionById(Long id);

    List<PermissionResponse> getAllPermissions(String search, Boolean isActive, String sort, boolean includeDeleted);

    PageResponse<PermissionResponse> getPermissionsPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted);

    List<GroupedPermissionResponse> getGroupedPermissions();
}