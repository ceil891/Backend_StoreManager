package org.example.storemanager.service.hrm.impl;

import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.hrm.department.CreateDepartmentHrmRequest;
import org.example.storemanager.dto.request.hrm.department.UpdateDepartmentHrmRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.hrm.department.CreateDepartmentHrmResponse;
import org.example.storemanager.dto.response.hrm.department.DeleteDepartmentHrmResponse;
import org.example.storemanager.dto.response.hrm.department.DepartmentHrmResponse;
import org.example.storemanager.dto.response.hrm.department.UpdateDepartmentHrmResponse;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.hrm.Department;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.hrm.DepartmentHrmRepository;
import org.example.storemanager.repository.system.UserRepository;
import org.example.storemanager.service.hrm.DepartmentHrmService;
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
public class DepartmentHrmHrmServiceImpl implements DepartmentHrmService {

    private final DepartmentHrmRepository departmentHrmRepository;
    private final UserRepository userRepository;

    @Autowired
    public DepartmentHrmHrmServiceImpl(DepartmentHrmRepository departmentHrmRepository, UserRepository userRepository) {
        this.departmentHrmRepository = departmentHrmRepository;
        this.userRepository = userRepository;
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Department", entityClass = Department.class)
    public CreateDepartmentHrmResponse create(CreateDepartmentHrmRequest request) {
        if (departmentHrmRepository.existsByDeptCodeAndIsDeletedFalse(request.getDeptCode())) {
            throw new DuplicateResourceException("Department", "deptCode", request.getDeptCode());
        }

        Department department = Department.builder()
                .deptCode(request.getDeptCode())
                .deptName(request.getDeptName())
                .description(request.getDescription())
                .manager(resolveManager(request.getManagerId()))
                .build();

        // Follow Attendance pattern: use isLocked as source of truth for active state
        department.setIsLocked(Boolean.FALSE.equals(request.getIsActive()));
        department.setIsDeleted(false);
        department.setCreatedBy(getCurrentUsername());

        Department saved = departmentHrmRepository.save(department);
        return mapToCreateResponse(saved);
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "Department", entityClass = Department.class)
    public UpdateDepartmentHrmResponse update(Long id, UpdateDepartmentHrmRequest request) {
        Department department = departmentHrmRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Only check deptCode uniqueness if it's being changed and is not null/blank
        if (request.getDeptCode() != null && !request.getDeptCode().isBlank() &&
            !department.getDeptCode().equals(request.getDeptCode()) && 
            departmentHrmRepository.existsByDeptCodeAndIdNotAndIsDeletedFalse(request.getDeptCode(), id)) {
            throw new DuplicateResourceException("Department", "deptCode", request.getDeptCode());
        }

        if (request.getDeptCode() != null && !request.getDeptCode().isBlank()) {
            department.setDeptCode(request.getDeptCode());
        }
        if (request.getDeptName() != null && !request.getDeptName().isBlank()) {
            department.setDeptName(request.getDeptName());
        }
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }
        if (request.getManagerId() != null) {
            department.setManager(resolveManager(request.getManagerId()));
        }
        if (request.getIsActive() != null) {
            department.setIsLocked(!request.getIsActive());
        }
        department.setUpdatedBy(getCurrentUsername());

        return mapToUpdateResponse(departmentHrmRepository.save(department));
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Department", entityClass = Department.class)
    public DeleteDepartmentHrmResponse delete(Long id) {
        Department department = departmentHrmRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        requireInactiveBeforeDelete(department, department.getDeptCode());
        applySoftDelete(department);
        // Attendance deletes rely on isLocked/isDeleted; isActive field remains but responses derive active from isLocked
        Department deleted = departmentHrmRepository.save(department);

        return DeleteDepartmentHrmResponse.builder()
                .id(deleted.getId())
                .deptCode(deleted.getDeptCode())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "Department", entityClass = Department.class)
    public UpdateDepartmentHrmResponse updateStatus(Long id, Boolean isActive) {
        Department department = departmentHrmRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Attendance pattern: update isLocked to reflect active flag
        department.setIsLocked(!isActive);
        department.setUpdatedBy(getCurrentUsername());
        return mapToUpdateResponse(departmentHrmRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentHrmResponse getById(Long id) {
        Department department = departmentHrmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return mapToResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentHrmResponse> getAll(String search, Boolean isActive, String sort, boolean includeDeleted) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, parseSort(sort, "deptName"));
        return departmentHrmRepository.findAllFiltered(search, isActive, includeDeleted, pageable)
                .getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentHrmResponse> getPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort, "deptName"));
        Page<Department> pageResult = departmentHrmRepository.findAllFiltered(search, isActive, includeDeleted, pageable);
        List<DepartmentHrmResponse> content = pageResult.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

        return PageResponse.<DepartmentHrmResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    private User resolveManager(Long managerId) {
        if (managerId == null) {
            return null;
        }
        return userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", managerId));
    }

    private DepartmentHrmResponse mapToResponse(Department department) {
        return DepartmentHrmResponse.builder()
                .id(department.getId())
                .deptCode(department.getDeptCode())
                .deptName(department.getDeptName())
                .description(department.getDescription())
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .managerName(department.getManager() != null ? department.getManager().getFullName() : null)
                .isActive(isActive(department.getIsLocked()))
                .isDeleted(department.getIsDeleted())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }

    private CreateDepartmentHrmResponse mapToCreateResponse(Department department) {
        return CreateDepartmentHrmResponse.builder()
                .id(department.getId())
                .deptCode(department.getDeptCode())
                .deptName(department.getDeptName())
                .description(department.getDescription())
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .isActive(isActive(department.getIsLocked()))
                .createdAt(department.getCreatedAt())
                .createdBy(department.getCreatedBy())
                .build();
    }

    private UpdateDepartmentHrmResponse mapToUpdateResponse(Department department) {
        return UpdateDepartmentHrmResponse.builder()
                .id(department.getId())
                .deptCode(department.getDeptCode())
                .deptName(department.getDeptName())
                .description(department.getDescription())
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .isActive(isActive(department.getIsLocked()))
                .updatedAt(department.getUpdatedAt())
                .updatedBy(department.getUpdatedBy())
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
