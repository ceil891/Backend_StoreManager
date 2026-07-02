package org.example.storemanager.service.partnerarea.area.Impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.partnerarea.area.CreateAreaRequest;
import org.example.storemanager.dto.response.partnerarea.area.*;
import org.example.storemanager.entity.partnerarea.Area;
import org.example.storemanager.repository.partnerarea.AreaRepository;
import org.example.storemanager.service.partnerarea.area.AreaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {

    private final AreaRepository repository;
    private final RestTemplate restTemplate;

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
    }

    @Override
    public AreaListResponse create(CreateAreaRequest req) {
        if (repository.existsByAreaCode(req.getAreaCode())) throw new RuntimeException("Mã đã tồn tại");

        Area parent = (req.getParentId() != null) ? repository.findById(req.getParentId()).orElse(null) : null;
        Area area = Area.builder()
                .areaCode(req.getAreaCode())
                .areaName(req.getAreaName())
                .level(req.getLevel())
                .parent(parent)
                .isActive(true)
                .build();
        area.setCreatedBy(getCurrentUsername());
        return mapToResponse(repository.save(area));
    }

    @Override
    public Page<AreaListResponse> getAll(Boolean isActive, Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public List<AreaTreeResponse> getTree() {
        List<Area> all = repository.findAll();
        return all.stream().filter(a -> a.getParent() == null)
                .map(a -> buildTree(a, all)).collect(Collectors.toList());
    }

    private AreaTreeResponse buildTree(Area area, List<Area> all) {
        AreaTreeResponse node = new AreaTreeResponse(area);
        node.setChildren(all.stream()
                .filter(a -> a.getParent() != null && a.getParent().getId().equals(area.getId()))
                .map(a -> buildTree(a, all)).collect(Collectors.toList()));
        return node;
    }

    @Override
    public void syncDataFromPublicApi() {
        String url = "https://provinces.open-api.vn/api/?depth=1";
        List<Map<String, Object>> data = restTemplate.getForObject(url, List.class);
        if (data == null) return;
        for (Map<String, Object> map : data) {
            String code = map.get("code").toString();
            if (!repository.existsByAreaCode(code)) {
                repository.save(Area.builder().areaCode(code).areaName(map.get("name").toString()).level(1).isActive(true).build());
            }
        }
    }

    @Override
    public void delete(Long id) {
        Area a = repository.findById(id).orElseThrow();
        if (Boolean.TRUE.equals(a.getIsActive())) throw new RuntimeException("Phải khóa trước khi xóa");
        a.setIsDeleted(true);
        a.setDeletedAt(LocalDateTime.now());
        a.setDeletedBy(getCurrentUsername());
        repository.save(a);
    }

    // Các hàm còn lại cần thực hiện nốt
    @Override public AreaListResponse update(Long id, CreateAreaRequest req) { return null; }
    @Override public void updateStatus(Long id) { /* Logic bật tắt */ }

    private AreaListResponse mapToResponse(Area a) {
        return AreaListResponse.builder()
                .id(a.getId())
                .areaCode(a.getAreaCode())
                .areaName(a.getAreaName())
                .level(a.getLevel())
                .parentName(a.getParent() != null ? a.getParent().getAreaName() : null)
                .isActive(a.getIsActive())
                .build();
    }
}