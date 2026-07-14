package org.example.storemanager.service.hrm.impl;

import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.hrm.position.CreatePositionRequest;
import org.example.storemanager.dto.request.hrm.position.UpdatePositionRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.hrm.position.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.hrm.Department;
import org.example.storemanager.entity.hrm.Position;
import org.example.storemanager.entity.system.Role;
import org.example.storemanager.enums.hrm.PositionRank;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.hrm.DepartmentHrmRepository;
import org.example.storemanager.repository.hrm.PositionRepository;
import org.example.storemanager.repository.system.RoleRepository;
import org.example.storemanager.service.hrm.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentHrmRepository departmentHrmRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public PositionServiceImpl(PositionRepository positionRepository,
                               DepartmentHrmRepository departmentHrmRepository,
                               RoleRepository roleRepository) {
        this.positionRepository = positionRepository;
        this.departmentHrmRepository = departmentHrmRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Position", entityClass = Position.class)
    public CreatePositionResponse create(CreatePositionRequest request) {
        String generatedCode = generateNextPositionCode();

        PositionRank positionRank = null;
        if (request.getPositionRank() != null && !request.getPositionRank().isBlank()) {
            positionRank = PositionRank.valueOf(request.getPositionRank().toUpperCase());
        }

        Role managementStatus = null;
        if (request.getManagementStatusId() != null) {
            managementStatus = roleRepository.findByIdAndIsDeletedFalse(request.getManagementStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getManagementStatusId()));
        }

        Position position = Position.builder()
                .positionCode(generatedCode)
                .positionName(request.getPositionName())
                .baseSalary(request.getBaseSalary())
                .department(resolveDepartment(request.getDepartmentId()))
                .positionRank(positionRank)
                .managementStatus(managementStatus)
                .description(request.getDescription())
                .build();

        position.setIsLocked(Boolean.FALSE.equals(request.getIsActive()));
        position.setIsDeleted(false);
        position.setCreatedBy(getCurrentUsername());

        return mapToCreateResponse(positionRepository.save(position));
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "Position", entityClass = Position.class)
    public UpdatePositionResponse update(Long id, UpdatePositionRequest request) {
        Position position = positionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));

        if (request.getPositionName() != null && !request.getPositionName().isBlank()) {
            position.setPositionName(request.getPositionName());
        }
        if (request.getBaseSalary() != null) {
            position.setBaseSalary(request.getBaseSalary());
        }
        if (request.getDepartmentId() != null) {
            position.setDepartment(resolveDepartment(request.getDepartmentId()));
        }
        if (request.getPositionRank() != null && !request.getPositionRank().isBlank()) {
            position.setPositionRank(PositionRank.valueOf(request.getPositionRank().toUpperCase()));
        }
        if (request.getManagementStatusId() != null) {
            Role managementStatus = roleRepository.findByIdAndIsDeletedFalse(request.getManagementStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getManagementStatusId()));
            position.setManagementStatus(managementStatus);
        }
        if (request.getDescription() != null) {
            position.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            position.setIsLocked(!request.getIsActive());
        }
        position.setUpdatedBy(getCurrentUsername());

        return mapToUpdateResponse(positionRepository.save(position));
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Position", entityClass = Position.class)
    public DeletePositionResponse delete(Long id) {
        Position position = positionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));

        requireInactiveBeforeDelete(position, position.getPositionCode());
        applySoftDelete(position);
        Position deleted = positionRepository.save(position);

        return DeletePositionResponse.builder()
                .id(deleted.getId())
                .positionCode(deleted.getPositionCode())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "Position", entityClass = Position.class)
    public UpdatePositionResponse updateStatus(Long id, Boolean isActive) {
        Position position = positionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));

        position.setIsLocked(!isActive);
        position.setUpdatedBy(getCurrentUsername());
        return mapToUpdateResponse(positionRepository.save(position));
    }

    @Override
    @Transactional(readOnly = true)
    public PositionResponse getById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));
        return mapToResponse(position);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponse> getAll(String search, Boolean isActive, Long departmentId, String sort, boolean includeDeleted) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, parseSort(sort, "positionName"));
        return positionRepository.findAllFiltered(search, isActive, departmentId, includeDeleted, pageable)
                .getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PositionResponse> getPaginated(String search, Boolean isActive, Long departmentId, int page, int size, String sort, boolean includeDeleted) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort, "positionName"));
        Page<Position> pageResult = positionRepository.findAllFiltered(search, isActive, departmentId, includeDeleted, pageable);
        List<PositionResponse> content = pageResult.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

        return PageResponse.<PositionResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionDropdownResponse> getDropdownList() {
        return positionRepository.findAllByIsDeletedFalseAndIsLockedFalseOrderByPositionNameAsc()
                .stream()
                .map(position -> PositionDropdownResponse.builder()
                        .id(position.getId())
                        .positionName(position.getPositionName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponse> search(String keyword) {
        return positionRepository.findByPositionCodeContainsIgnoreCaseOrPositionNameContainsIgnoreCaseAndIsDeletedFalse(keyword, keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Department resolveDepartment(Long departmentId) {
        return departmentHrmRepository.findByIdAndIsDeletedFalse(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
    }

    private String generateNextPositionCode() {
        return positionRepository.findMaxPositionCodeAsInteger()
                .map(maxCode -> String.valueOf(maxCode + 1))
                .orElse("1");
    }

    private PositionResponse mapToResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .positionCode(position.getPositionCode())
                .positionName(position.getPositionName())
                .baseSalary(position.getBaseSalary())
                .departmentId(position.getDepartment().getId())
                .departmentName(position.getDepartment().getDeptName())
                .positionRank(position.getPositionRank() != null ? position.getPositionRank().name() : null)
                .managementStatus(position.getManagementStatus() != null ? position.getManagementStatus().getRoleName() : null)
                .description(position.getDescription())
                .isActive(isActive(position.getIsLocked()))
                .isDeleted(position.getIsDeleted())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }

    private CreatePositionResponse mapToCreateResponse(Position position) {
        return CreatePositionResponse.builder()
                .id(position.getId())
                .positionCode(position.getPositionCode())
                .positionName(position.getPositionName())
                .baseSalary(position.getBaseSalary())
                .departmentId(position.getDepartment().getId())
                .positionRank(position.getPositionRank() != null ? position.getPositionRank().name() : null)
                .managementStatus(position.getManagementStatus() != null ? position.getManagementStatus().getRoleName() : null)
                .description(position.getDescription())
                .isActive(isActive(position.getIsLocked()))
                .createdAt(position.getCreatedAt())
                .createdBy(position.getCreatedBy())
                .build();
    }

    private UpdatePositionResponse mapToUpdateResponse(Position position) {
        return UpdatePositionResponse.builder()
                .id(position.getId())
                .positionCode(position.getPositionCode())
                .positionName(position.getPositionName())
                .baseSalary(position.getBaseSalary())
                .departmentId(position.getDepartment().getId())
                .positionRank(position.getPositionRank() != null ? position.getPositionRank().name() : null)
                .managementStatus(position.getManagementStatus() != null ? position.getManagementStatus().getRoleName() : null)
                .description(position.getDescription())
                .isActive(isActive(position.getIsLocked()))
                .updatedAt(position.getUpdatedAt())
                .updatedBy(position.getUpdatedBy())
                .build();
    }

    // ---- Hrm support methods (inlined) ----
    private static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private static Sort parseSort(String sortParam, String defaultProperty) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by(defaultProperty).descending();
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private static boolean isActive(Boolean isLocked) {
        return !Boolean.TRUE.equals(isLocked);
    }

    private static void applySoftDelete(BaseEntity entity) {
        String username = getCurrentUsername();
        entity.setIsDeleted(true);
        entity.setIsLocked(true);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(username);
        entity.setUpdatedBy(username);
    }

    private static void requireInactiveBeforeDelete(BaseEntity entity, String label) {
        if (isActive(entity.getIsLocked())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa '" + label + "' vì bản ghi vẫn đang HOẠT ĐỘNG. Vui lòng tắt hoạt động trước."
            );
        }
    }

    private static <E extends Enum<E>> String requireEnumName(String value, Class<E> enumClass, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldLabel + " không được để trống");
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldLabel + " không hợp lệ. Giá trị cho phép: " + formatAllowedEnumValues(enumClass)
            );
        }
    }

    private static <E extends Enum<E>> String parseOptionalEnumName(String value, Class<E> enumClass, String fieldLabel) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireEnumName(value, enumClass, fieldLabel);
    }

    private static <E extends Enum<E>> String formatAllowedEnumValues(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
