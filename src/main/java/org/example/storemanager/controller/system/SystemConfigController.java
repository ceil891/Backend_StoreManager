package org.example.storemanager.controller.system;

import jakarta.validation.Valid;
import org.example.storemanager.dto.request.system.systemconfig.CreateSystemConfigRequest;
import org.example.storemanager.dto.request.system.systemconfig.UpdateSystemConfigRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.system.systemconfig.*;
import org.example.storemanager.service.system.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/system/systemconfigs")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Autowired
    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:systemconfig:create')")
    public ResponseEntity<ApiResponse<CreateSystemConfigResponse>> createConfig(@Valid @RequestBody CreateSystemConfigRequest request) {
        CreateSystemConfigResponse response = systemConfigService.createConfig(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:systemconfig:update')")
    public ResponseEntity<ApiResponse<UpdateSystemConfigResponse>> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        UpdateSystemConfigResponse response = systemConfigService.updateConfig(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật cấu hình thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('system:systemconfig:update-status')")
    public ResponseEntity<ApiResponse<UpdateSystemConfigResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        UpdateSystemConfigResponse response = systemConfigService.updateStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái cấu hình thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:systemconfig:delete')")
    public ResponseEntity<ApiResponse<DeleteSystemConfigResponse>> deleteConfig(@PathVariable Long id) {
        DeleteSystemConfigResponse response = systemConfigService.deleteConfig(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa cấu hình thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:systemconfig:view')")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> getConfigById(@PathVariable Long id) {
        SystemConfigResponse response = systemConfigService.getConfigById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/key/{configKey}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:systemconfig:view')")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> getConfigByKey(@PathVariable String configKey) {
        SystemConfigResponse response = systemConfigService.getConfigByKey(configKey);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:systemconfig:view')")
    public ResponseEntity<ApiResponse<?>> getConfigs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "configKey,asc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    systemConfigService.getConfigsPaginated(search, isActive, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                    systemConfigService.getAllConfigs(search, isActive, sort, includeDeleted)));
        }
    }
}