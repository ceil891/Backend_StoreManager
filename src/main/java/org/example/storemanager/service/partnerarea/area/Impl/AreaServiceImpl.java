package org.example.storemanager.service.partnerarea.area.Impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.partnerarea.area.CreateAreaRequest;
import org.example.storemanager.dto.response.partnerarea.area.AreaListResponse;
import org.example.storemanager.entity.partnerarea.Area;
import org.example.storemanager.repository.partnerarea.AreaRepository;
import org.example.storemanager.service.partnerarea.area.AreaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {

    private final AreaRepository repository;

    @Override
    public Page<AreaListResponse> getAll(Boolean isActive, Pageable pageable) {
        Page<Area> areas = (isActive == null) ? repository.findAll(pageable) : repository.findByIsActive(isActive, pageable);
        return areas.map(this::mapToResponse);
    }

    @Override
    public AreaListResponse create(CreateAreaRequest req) {
        long count = repository.count();
        String newCode = String.format("AREA_%03d", count + 1);

        // 2. Kiểm tra xem mã sinh ra đã bị trùng chưa (đề phòng xóa rồi tạo lại)
        while (repository.existsByAreaCode(newCode)) {
            count++;
            newCode = String.format("AREA_%03d", count + 1);
        }
        Area parent = (req.getParentId() != null) ? repository.findById(req.getParentId()).orElse(null) : null;
        Area area = Area.builder()
                .areaCode(newCode)
                .areaName(req.getAreaName())
                .level(req.getLevel())
                .parent(parent)
                .build();
        area.setIsActive(true);
        return mapToResponse(repository.save(area));
    }

    @Override
    public AreaListResponse update(Long id, CreateAreaRequest req) {
        Area area = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khu vực"));
        area.setAreaName(req.getAreaName());
        area.setLevel(req.getLevel());
        if (req.getParentId() != null) {
            area.setParent(repository.findById(req.getParentId()).orElse(null));
        }
        return mapToResponse(repository.save(area));
    }

    @Override
    public void updateStatus(Long id) {
        Area a = repository.findById(id).orElseThrow();
        a.setIsActive(!a.getIsActive());
        repository.save(a);
    }

    @Override
    public Boolean checkIsActive(Long id) {
        return repository.findById(id).orElseThrow().getIsActive();
    }

    @Override
    public void delete(Long id) {
        Area a = repository.findById(id).orElseThrow();
        if (Boolean.TRUE.equals(a.getIsActive())) throw new RuntimeException("Phải khóa trước khi xóa");
        a.setIsDeleted(true);
        repository.save(a);
    }

    // Hàm tiện ích để map dữ liệu
    private AreaListResponse mapToResponse(Area a) {
        return AreaListResponse.builder()
                .id(a.getId())
                .areaCode(a.getAreaCode())
                .areaName(a.getAreaName())
                .level(a.getLevel())
                .parentName(a.getParent() != null ? a.getParent().getAreaName() : "—")
                .isActive(a.getIsActive())
                .build();
    }
}