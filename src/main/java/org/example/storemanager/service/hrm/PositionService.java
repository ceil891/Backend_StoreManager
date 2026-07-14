package org.example.storemanager.service.hrm;

import org.example.storemanager.dto.request.hrm.position.CreatePositionRequest;
import org.example.storemanager.dto.request.hrm.position.UpdatePositionRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.hrm.position.*;

import java.util.List;

public interface PositionService {

    CreatePositionResponse create(CreatePositionRequest request);

    UpdatePositionResponse update(Long id, UpdatePositionRequest request);

    DeletePositionResponse delete(Long id);

    UpdatePositionResponse updateStatus(Long id, Boolean isActive);

    PositionResponse getById(Long id);

    List<PositionResponse> getAll(String search, Boolean isActive, Long departmentId, String sort, boolean includeDeleted);

    PageResponse<PositionResponse> getPaginated(String search, Boolean isActive, Long departmentId, int page, int size, String sort, boolean includeDeleted);

    List<PositionDropdownResponse> getDropdownList();

    List<PositionResponse> search(String keyword);
}
