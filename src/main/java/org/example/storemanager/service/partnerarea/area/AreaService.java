package org.example.storemanager.service.partnerarea.area;

import org.example.storemanager.dto.request.partnerarea.area.CreateAreaRequest;
import org.example.storemanager.dto.response.partnerarea.area.AreaListResponse;
import org.example.storemanager.dto.response.partnerarea.area.AreaTreeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AreaService {
    // 1. Quản lý cơ bản
    AreaListResponse create(CreateAreaRequest req);
    AreaListResponse update(Long id, CreateAreaRequest req);
    void updateStatus(Long id); // Bật/Tắt trạng thái
    void delete(Long id);       // Xóa mềm

    // 2. Tìm kiếm & Hiển thị
    Page<AreaListResponse> getAll(Boolean isActive, Pageable pageable);
    List<AreaTreeResponse> getTree();

    // 3. Hệ thống
    void syncDataFromPublicApi();
}