package org.example.storemanager.service.system.impl;

import jakarta.persistence.criteria.Predicate;
import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.system.systemconfig.CreateSystemConfigRequest;
import org.example.storemanager.dto.request.system.systemconfig.UpdateSystemConfigRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.system.systemconfig.*;
import org.example.storemanager.entity.system.SystemConfig;
import org.example.storemanager.enums.ErrorCode;
import org.example.storemanager.exception.BusinessException;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.system.SystemConfigRepository;
import org.example.storemanager.service.system.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    @Autowired
    public SystemConfigServiceImpl(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "SystemConfig", entityClass = SystemConfig.class)
    public CreateSystemConfigResponse createConfig(CreateSystemConfigRequest request) {
        if (systemConfigRepository.existsByConfigKey(request.getConfigKey())) {
            throw new DuplicateResourceException("SystemConfig", "configKey", request.getConfigKey());
        }

        SystemConfig config = new SystemConfig();
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setDescription(request.getDescription());
        config.setCreatedBy(getCurrentUsername());

        SystemConfig savedConfig = systemConfigRepository.save(config);
        return mapToCreateResponse(savedConfig);
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "SystemConfig", entityClass = SystemConfig.class)
    public UpdateSystemConfigResponse updateConfig(Long id, UpdateSystemConfigRequest request) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "id", id));

        config.setConfigValue(request.getConfigValue());
        config.setDescription(request.getDescription());
        config.setUpdatedBy(getCurrentUsername());

        SystemConfig updatedConfig = systemConfigRepository.save(config);
        return mapToUpdateResponse(updatedConfig);
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "SystemConfig", entityClass = SystemConfig.class)
    public UpdateSystemConfigResponse updateStatus(Long id, Boolean isActive) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "id", id));

        /* * LƯU Ý: Entity SystemConfig hiện tại chưa có trường `isActive`.
         * Nếu bạn muốn dùng, hãy mở file SystemConfig.java và thêm:
         * @Column(name = "is_active", columnDefinition = "boolean default true")
         * private Boolean isActive = true;
         * * Sau khi thêm, bạn có thể bỏ comment dòng code bên dưới:
         */
        // config.setIsActive(isActive);

        config.setUpdatedBy(getCurrentUsername());
        SystemConfig updatedConfig = systemConfigRepository.save(config);
        return mapToUpdateResponse(updatedConfig);
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "SystemConfig", entityClass = SystemConfig.class)
    public DeleteSystemConfigResponse deleteConfig(Long id) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "id", id));

        config.setIsDeleted(true);
        config.setDeletedAt(LocalDateTime.now());
        config.setDeletedBy(getCurrentUsername());

        SystemConfig deletedConfig = systemConfigRepository.save(config);

        return DeleteSystemConfigResponse.builder()
                .id(deletedConfig.getId())
                .configKey(deletedConfig.getConfigKey())
                .isDeleted(deletedConfig.getIsDeleted())
                .deletedAt(deletedConfig.getDeletedAt())
                .deletedBy(deletedConfig.getDeletedBy())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfigResponse getConfigById(Long id) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "id", id));
        return mapToResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfigResponse getConfigByKey(String configKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "configKey", configKey));
        return mapToResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemConfigResponse> getAllConfigs(String search, Boolean isActive, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Specification<SystemConfig> spec = buildSpecification(search, isActive, includeDeleted);
        List<SystemConfig> configs = systemConfigRepository.findAll(spec, sorting);
        return configs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SystemConfigResponse> getConfigsPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Specification<SystemConfig> spec = buildSpecification(search, isActive, includeDeleted);

        Page<SystemConfig> pageResult = systemConfigRepository.findAll(spec, pageable);

        List<SystemConfigResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<SystemConfigResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    // ================= HELPER METHODS (Lấy thông tin User & Xử lý query) =================

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by("id").descending();
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private Specification<SystemConfig> buildSpecification(String search, Boolean isActive, boolean includeDeleted) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!includeDeleted) {
                predicates.add(cb.or(
                        cb.isNull(root.get("isDeleted")),
                        cb.isFalse(root.get("isDeleted"))
                ));
            }

            // Mở comment này nếu Entity có trường isActive
            /*
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            */

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("configKey")), searchPattern),
                        cb.like(cb.lower(root.get("configValue")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ================= MAPPERS =================

    private SystemConfigResponse mapToResponse(SystemConfig config) {
        return SystemConfigResponse.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                // .isActive(config.getIsActive()) // Mở comment nếu đã thêm isActive
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    private CreateSystemConfigResponse mapToCreateResponse(SystemConfig config) {
        return CreateSystemConfigResponse.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .createdBy(config.getCreatedBy())
                .build();
    }

    private UpdateSystemConfigResponse mapToUpdateResponse(SystemConfig config) {
        return UpdateSystemConfigResponse.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .updatedAt(config.getUpdatedAt())
                .updatedBy(config.getUpdatedBy())
                .build();
    }
}