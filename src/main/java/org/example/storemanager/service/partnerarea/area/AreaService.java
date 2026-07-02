package org.example.storemanager.service.partnerarea.area;

import org.example.storemanager.dto.request.partnerarea.area.CreateAreaRequest;
import org.example.storemanager.dto.response.partnerarea.area.AreaListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AreaService {
    Page<AreaListResponse> getAll(Boolean isActive, Pageable pageable);
    AreaListResponse create(CreateAreaRequest req);

    AreaListResponse update(Long id, CreateAreaRequest req);

    void updateStatus(Long id);

    Boolean checkIsActive(Long id);

    void delete(Long id);
}