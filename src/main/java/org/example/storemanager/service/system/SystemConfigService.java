package org.example.storemanager.service.system;

import org.example.storemanager.dto.request.system.systemconfig.CreateSystemConfigRequest;
import org.example.storemanager.dto.request.system.systemconfig.UpdateSystemConfigRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.systemconfig.*;
import java.util.List;

public interface SystemConfigService {
    CreateSystemConfigResponse createConfig(CreateSystemConfigRequest request);
    UpdateSystemConfigResponse updateConfig(Long id, UpdateSystemConfigRequest request);
    UpdateSystemConfigResponse updateStatus(Long id, Boolean isActive);
    DeleteSystemConfigResponse deleteConfig(Long id);
    SystemConfigResponse getConfigById(Long id);
    SystemConfigResponse getConfigByKey(String configKey);
    PageResponse<SystemConfigResponse> getConfigsPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted);
    List<SystemConfigResponse> getAllConfigs(String search, Boolean isActive, String sort, boolean includeDeleted);
}