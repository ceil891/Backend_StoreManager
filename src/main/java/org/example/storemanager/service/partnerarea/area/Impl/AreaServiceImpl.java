package org.example.storemanager.service.partnerarea.area.Impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.external.DistrictDTO;
import org.example.storemanager.dto.external.ProvinceDTO;
import org.example.storemanager.dto.external.WardDTO;
import org.example.storemanager.dto.request.partnerarea.area.CreateAreaRequest;
import org.example.storemanager.dto.response.partnerarea.area.AreaDisplayDTO;
import org.example.storemanager.dto.response.partnerarea.area.AreaListResponse;
import org.example.storemanager.entity.partnerarea.Area;
import org.example.storemanager.repository.partnerarea.AreaRepository;
import org.example.storemanager.service.partnerarea.area.AreaService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {
    private final RestTemplate restTemplate;
    private final AreaRepository repository;

    // --- MAPPING ---
    // Trong AreaServiceImpl.java
    private AreaListResponse mapToResponse(Area a) {
        // Chỉ hiển thị tên chính, không kèm chữ "PROVINCE" hay "DISTRICT"
        // Nếu muốn hiển thị "Tỉnh Hà Nội", "Quận Ba Đình" thì dùng logic if/else
        String displayName = a.getAreaName();

        // Tùy chọn: Thêm tiền tố thân thiện (nếu cậu muốn)
        if ("PROVINCE".equals(a.getType())) {
            displayName = "Tỉnh " + a.getAreaName();
        } else if ("DISTRICT".equals(a.getType())) {
            displayName = "Quận/Huyện " + a.getAreaName();
        } else if ("WARD".equals(a.getType())) {
            displayName = "Phường/Xã " + a.getAreaName();
        }

        return AreaListResponse.builder()
                .id(a.getId())
                .areaCode(a.getAreaCode())
                .areaName(a.getAreaName())
                .fullName(displayName) // Dùng tên đã xử lý
                .type(a.getType())
                .level(a.getLevel())
                .isActive(a.getIsActive())
                .parentName(a.getParent() != null ? a.getParent().getAreaName() : null)
                .children(new ArrayList<>())
                .build();
    }

    private AreaDisplayDTO mapToDisplay(Area a) {
        return AreaDisplayDTO.builder()
                .id(a.getId())
                .name(a.getAreaName())
                .code(a.getAreaCode())
                .type(a.getType())
                .level(a.getLevel())
                .children(a.getChildren() != null ?
                        a.getChildren().stream().map(this::mapToDisplay).collect(Collectors.toList()) : null)
                .build();
    }

    // --- TREE & SYNC ---
    @Override
    @Transactional(readOnly = true)
    public List<AreaListResponse> getTree() {
        return repository.findRootAreasWithChildren().stream()
                .map(this::mapToTree)
                .collect(Collectors.toList());
    }

    private AreaListResponse mapToTree(Area a) {
        AreaListResponse dto = mapToResponse(a);
        if (a.getChildren() != null) {
            dto.setChildren(a.getChildren().stream().map(this::mapToTree).collect(Collectors.toList()));
        }
        return dto;
    }

    @Override
    @Transactional
    public void syncDataFromPublicApi() {
        String url = "https://provinces.open-api.vn/api/?depth=3";
        List<ProvinceDTO> provinces = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ProvinceDTO>>() {}).getBody();

        if (provinces == null) return;

        Map<String, Area> areaMap = new HashMap<>();
        repository.findAll().forEach(a -> areaMap.put(a.getAreaName() + "_" + a.getLevel(), a));

        List<Area> newAreas = new ArrayList<>();

        for (ProvinceDTO p : provinces) {
            Area province = areaMap.computeIfAbsent(p.getName() + "_1", k -> {
                Area a = Area.builder().areaName(p.getName()).level(1).type("PROVINCE").areaCode("PROV_" + p.getCode()).isActive(true).build();
                newAreas.add(a);
                return a;
            });

            if (p.getDistricts() != null) {
                for (DistrictDTO d : p.getDistricts()) {
                    Area district = areaMap.computeIfAbsent(d.getName() + "_2", k -> {
                        Area a = Area.builder().areaName(d.getName()).level(2).type("DISTRICT")
                                .parent(province).areaCode("DIST_" + d.getCode()).isActive(true).build();
                        newAreas.add(a);
                        return a;
                    });

                    if (d.getWards() != null) {
                        for (WardDTO w : d.getWards()) {
                            areaMap.computeIfAbsent(w.getName() + "_3", k -> {
                                Area a = Area.builder().areaName(w.getName()).level(3).type("WARD")
                                        .parent(district).areaCode("WARD_" + w.getCode()).isActive(true).build();
                                newAreas.add(a);
                                return a;
                            });
                        }
                    }
                }
            }
        }
        repository.saveAll(newAreas);
    }

    // --- CRUD ---
    @Override
    public AreaListResponse create(CreateAreaRequest req) {
        Area parent = (req.getParentId() != null) ? repository.findById(req.getParentId()).orElse(null) : null;
        Area area = Area.builder()
                .areaCode(req.getCode())
                .areaName(req.getName())
                .level(req.getLevel())
                .type(req.getType())
                .parent(parent)
                .isActive(true)
                .build();
        return mapToResponse(repository.save(area));
    }

    @Override
    public AreaListResponse update(Long id, CreateAreaRequest req) {
        Area area = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        area.setAreaName(req.getName());
        area.setType(req.getType());
        area.setLevel(req.getLevel());
        return mapToResponse(repository.save(area));
    }

    @Override
    public AreaListResponse toggleStatus(Long id) {
        Area a = repository.findById(id).orElseThrow();
        a.setIsActive(!a.getIsActive());
        return mapToResponse(repository.save(a));
    }

    @Override
    public AreaListResponse delete(Long id) {
        Area a = repository.findById(id).orElseThrow();
        repository.delete(a);
        return mapToResponse(a);
    }

    // --- GETTERS ---
    @Override
    public Page<AreaListResponse> getAll(Pageable pageable, String search, String type) {
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public AreaListResponse getById(Long id) {
        return mapToResponse(repository.findById(id).orElseThrow());
    }

    @Override
    public List<AreaListResponse> getChildren(Long parentId) {
        return repository.findByParentId(parentId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<AreaListResponse> getByType(String type) {
        return repository.findByType(type).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<AreaListResponse> getDropdown() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public boolean exists(String code) {
        return repository.existsByAreaCode(code);
    }
}