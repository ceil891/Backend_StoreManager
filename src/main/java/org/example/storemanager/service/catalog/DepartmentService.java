package org.example.storemanager.service.catalog;

import org.example.storemanager.dto.request.catalog.department.CreateDepartmentRequest;
import org.example.storemanager.dto.request.catalog.department.UpdateDepartmentRequest;
import org.example.storemanager.dto.response.catalog.department.*;
import org.example.storemanager.dto.response.common.PageResponse;

import java.util.List;

public interface DepartmentService {
    CreateDepartmentResponse createDepartment(CreateDepartmentRequest request);

    UpdateDepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request);

    DeleteDepartmentResponse deleteDepartment(Long id);

    UpdateDepartmentResponse updateStatus(Long id, Boolean isActive);

    DepartmentResponse getDepartmentById(Long id);

    List<MapDepartmentResponse> getAllDepartments(
            String search,
            Boolean isActive,
            String sort,
            boolean includeDeleted);

    PageResponse<MapDepartmentResponse> getDepartmentsPaginated(
            String search,
            Boolean isActive,
            int page,
            int size,
            String sort,
            boolean includeDeleted);
}
