package org.example.storemanager.modules.catalog.service.impl;

import org.example.storemanager.shared.config.LogActivity;
import org.example.storemanager.modules.catalog.dto.request.department.CreateDepartmentRequest;
import org.example.storemanager.modules.catalog.dto.request.department.UpdateDepartmentRequest;
import org.example.storemanager.modules.catalog.dto.response.department.MapDepartmentResponse;
import org.example.storemanager.modules.catalog.dto.response.department.CreateDepartmentResponse;
import org.example.storemanager.modules.catalog.dto.response.department.DeleteDepartmentResponse;
import org.example.storemanager.modules.catalog.dto.response.department.UpdateDepartmentResponse;
import org.example.storemanager.modules.catalog.dto.response.department.DepartmentResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.catalog.entity.Department;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.DepartmentRepository;
import org.example.storemanager.modules.catalog.service.DepartmentService;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Department", entityClass = Department.class)
    public CreateDepartmentResponse createDepartment(CreateDepartmentRequest request) {
        if (departmentRepository.existsByDeptCodeAndIsDeletedFalse(request.getDeptCode())) {
            throw new DuplicateResourceException("Department", "deptCode", request.getDeptCode());
        }

        Department department = Department.builder()
                .deptCode(request.getDeptCode())
                .deptName(request.getDeptName())
                .description(request.getDescription())
                .build();

        department.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        department.setIsDeleted(false);
        department.setCreatedBy(getCurrentUsername());

        Department saved = departmentRepository.save(department);
        return mapToCreateResponse(saved);
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "Department", entityClass = Department.class)
    public UpdateDepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        if (departmentRepository.existsByDeptCodeAndIdNotAndIsDeletedFalse(request.getDeptCode(), id)) {
            throw new DuplicateResourceException("Department", "deptCode", request.getDeptCode());
        }

        department.setDeptCode(request.getDeptCode());
        department.setDeptName(request.getDeptName());
        department.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            department.setIsActive(request.getIsActive());
        }
        department.setUpdatedBy(getCurrentUsername());

        Department updated = departmentRepository.save(department);
        return mapToUpdateResponse(updated);
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Department", entityClass = Department.class)
    public DeleteDepartmentResponse deleteDepartment(Long id) {
        Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        if (Boolean.TRUE.equals(department.getIsActive())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa ngành hàng '" + department.getDeptCode() + "' vì ngành hàng này vẫn đang hoạt động . " +
                "Vui lòng tắt hoạt động trước, sau đó mới có thể xóa."
            );
        }

        String username = getCurrentUsername();
        department.setIsDeleted(true);
        department.setIsActive(false);
        department.setDeletedAt(LocalDateTime.now());
        department.setDeletedBy(username);
        department.setUpdatedBy(username);

        Department deleted = departmentRepository.save(department);
        return DeleteDepartmentResponse.builder()
                .id(deleted.getId())
                .deptCode(deleted.getDeptCode())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "Department", entityClass = Department.class)
    public UpdateDepartmentResponse updateStatus(Long id, Boolean isActive) {
        Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        department.setIsActive(isActive);
        department.setUpdatedBy(getCurrentUsername());

        Department updated = departmentRepository.save(department);
        return mapToUpdateResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return mapToResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MapDepartmentResponse> getAllDepartments(String search, Boolean isActive, String sort, boolean includeDeleted) {
        if (search != null && search.trim().isEmpty()) {
            search = null;
        }
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<Department> page = departmentRepository.findAllDepartmentsIncludeDeleted(search, isActive, includeDeleted, pageable);
        return page.getContent().stream()
                .map(this::mapToResponseAll)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MapDepartmentResponse> getDepartmentsPaginated(
            String search,
            Boolean isActive,
            int page,
            int size,
            String sort,
            boolean includeDeleted) {

        if (search != null && search.trim().isEmpty()) {
            search = null;
        }
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Department> pageResult =
                departmentRepository.findAllDepartmentsIncludeDeleted(
                        search,
                        isActive,
                        includeDeleted,
                        pageable);

        List<MapDepartmentResponse> content = pageResult.getContent()
                .stream()
                .map(this::mapToResponseAll)
                .collect(Collectors.toList());

        return PageResponse.<MapDepartmentResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by("id").descending();
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        if ("code".equalsIgnoreCase(property)) {
            property = "deptCode";
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .deptCode(department.getDeptCode())
                .deptName(department.getDeptName())
                .description(department.getDescription())
                .isActive(department.getIsActive())
                .createdAt(department.getCreatedAt())
                .createdBy(department.getCreatedBy())
                .updatedBy(department.getUpdatedBy())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
    private MapDepartmentResponse mapToResponseAll(Department department) {
        return MapDepartmentResponse.builder()
                .id(department.getId())
                .deptCode(department.getDeptCode())
                .deptName(department.getDeptName())
                .description(department.getDescription())
                .isActive(department.getIsActive())
                .createdAt(department.getCreatedAt())
                .createdBy(department.getCreatedBy())
                .updatedBy(department.getUpdatedBy())
                .updatedAt(department.getUpdatedAt())
                .isDeleted(department.getIsDeleted())
                .build();
    }

    private CreateDepartmentResponse mapToCreateResponse(Department department) {
        return CreateDepartmentResponse.builder()
                .id(department.getId())
                .deptCode(department.getDeptCode())
                .deptName(department.getDeptName())
                .description(department.getDescription())
                .isActive(department.getIsActive())
                .createdAt(department.getCreatedAt())
                .createdBy(department.getCreatedBy())
                .build();
    }

    private UpdateDepartmentResponse mapToUpdateResponse(Department department) {
        return UpdateDepartmentResponse.builder()
                .id(department.getId())
                .deptCode(department.getDeptCode())
                .deptName(department.getDeptName())
                .description(department.getDescription())
                .isActive(department.getIsActive())
                .updatedAt(department.getUpdatedAt())
                .updatedBy(department.getUpdatedBy())
                .build();
    }
}
