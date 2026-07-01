package org.example.storemanager.service.hrm;

import org.example.storemanager.dto.request.hrm.department.CreateDepartmentHrmRequest;
import org.example.storemanager.dto.request.hrm.department.UpdateDepartmentHrmRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.hrm.department.CreateDepartmentHrmResponse;
import org.example.storemanager.dto.response.hrm.department.DeleteDepartmentHrmResponse;
import org.example.storemanager.dto.response.hrm.department.DepartmentHrmResponse;
import org.example.storemanager.dto.response.hrm.department.UpdateDepartmentHrmResponse;

import java.util.List;

public interface DepartmentHrmService {

    CreateDepartmentHrmResponse create(CreateDepartmentHrmRequest request);

    UpdateDepartmentHrmResponse update(Long id, UpdateDepartmentHrmRequest request);

    DeleteDepartmentHrmResponse delete(Long id);

    UpdateDepartmentHrmResponse updateStatus(Long id, Boolean isActive);

    DepartmentHrmResponse getById(Long id);

    List<DepartmentHrmResponse> getAll(String search, Boolean isActive, String sort, boolean includeDeleted);

    PageResponse<DepartmentHrmResponse> getPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted);
}
