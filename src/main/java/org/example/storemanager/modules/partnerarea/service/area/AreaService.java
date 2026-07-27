package org.example.storemanager.modules.partnerarea.service.area;

import org.example.storemanager.modules.partnerarea.dto.request.area.CreateAreaRequest;
import org.example.storemanager.modules.partnerarea.dto.response.area.AreaListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AreaService {
    void syncDataFromPublicApi(); // API 1
    AreaListResponse create(CreateAreaRequest req); // API 2
    Page<AreaListResponse> getAll(Pageable pageable, String search, String type); // API 3
    AreaListResponse getById(Long id); // API 4
    AreaListResponse update(Long id, CreateAreaRequest req); // API 5
    AreaListResponse delete(Long id); // API 6
    AreaListResponse toggleStatus(Long id); // API 7
    List<AreaListResponse> getTree(); // API 8
    List<AreaListResponse> getChildren(Long parentId); // API 9
    List<AreaListResponse> getByType(String type); // API 10
    List<AreaListResponse> getDropdown(); // API 11
    boolean exists(String code); // API 12
}