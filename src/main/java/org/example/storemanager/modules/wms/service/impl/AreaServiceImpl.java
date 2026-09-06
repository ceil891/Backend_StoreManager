package org.example.storemanager.modules.wms.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.wms.dto.AreaDTO;
import org.example.storemanager.modules.wms.entity.Area;
import org.example.storemanager.modules.wms.entity.WarehouseZone;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.wms.repository.AreaRepository;
import org.example.storemanager.modules.wms.repository.WarehouseZoneRepository;
import org.example.storemanager.modules.wms.service.AreaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("wmsAreaService")
@RequiredArgsConstructor
@Transactional
public class AreaServiceImpl implements AreaService {

    private final AreaRepository areaRepository;
    private final WarehouseZoneRepository warehouseZoneRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AreaDTO.Response> getAllAreas() {
        return areaRepository.findAllActive().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaDTO.Response> getAreasByZoneId(Long zoneId) {
        return areaRepository.findByZone_IdAndIsDeletedFalse(zoneId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaDTO.Response> getAreasByBranchId(Long branchId) {
        return areaRepository.findActiveByBranch(branchId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AreaDTO.Response getAreaById(Long id) {
        Area area = areaRepository.findById(id)
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", id));
        return toResponse(area);
    }

    @Override
    public AreaDTO.Response createArea(AreaDTO.Request request) {
        if (areaRepository.existsByAreaCode(request.getAreaCode())) {
            throw new DuplicateResourceException("Area", "areaCode", request.getAreaCode());
        }

        WarehouseZone zone = warehouseZoneRepository.findByIdAndIsDeletedFalse(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseZone", "id", request.getZoneId()));

        Area area = Area.builder()
                .areaCode(request.getAreaCode())
                .areaName(request.getAreaName())
                .description(request.getDescription())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .addressDetail(request.getAddressDetail())
                .areaSizeM2(request.getAreaSizeM2())
                .storageCondition(request.getStorageCondition())
                .isActive(Boolean.TRUE.equals(request.getIsActive()))
                .zone(zone)
                .build();
        area.setIsDeleted(false);

        return toResponse(areaRepository.save(area));
    }

    @Override
    public AreaDTO.Response updateArea(Long id, AreaDTO.Request request) {
        Area area = areaRepository.findById(id)
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", id));

        if (request.getZoneId() != null) {
            WarehouseZone zone = warehouseZoneRepository.findByIdAndIsDeletedFalse(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("WarehouseZone", "id", request.getZoneId()));
            area.setZone(zone);
        }

        area.setAreaCode(request.getAreaCode());
        area.setAreaName(request.getAreaName());
        area.setDescription(request.getDescription());
        area.setProvince(request.getProvince());
        area.setDistrict(request.getDistrict());
        area.setWard(request.getWard());
        area.setAddressDetail(request.getAddressDetail());
        if (request.getAreaSizeM2() != null) area.setAreaSizeM2(request.getAreaSizeM2());
        if (request.getStorageCondition() != null) area.setStorageCondition(request.getStorageCondition());
        if (request.getIsActive() != null) area.setIsActive(request.getIsActive());

        return toResponse(areaRepository.save(area));
    }

    @Override
    public void deleteArea(Long id) {
        Area area = areaRepository.findById(id)
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", id));
        area.setIsDeleted(true);
        areaRepository.save(area);
    }

    private AreaDTO.Response toResponse(Area area) {
        WarehouseZone zone = area.getZone();
        return AreaDTO.Response.builder()
                .id(area.getId())
                .areaCode(area.getAreaCode())
                .areaName(area.getAreaName())
                .description(area.getDescription())
                .province(area.getProvince())
                .district(area.getDistrict())
                .ward(area.getWard())
                .addressDetail(area.getAddressDetail())
                .areaSizeM2(area.getAreaSizeM2())
                .storageCondition(area.getStorageCondition())
                .isActive(area.getIsActive())
                .zoneId(zone != null ? zone.getId() : null)
                .zoneCode(zone != null ? zone.getZoneCode() : null)
                .zoneName(zone != null ? zone.getZoneName() : null)
                .branchId(zone != null && zone.getBranch() != null ? zone.getBranch().getId() : null)
                .branchName(zone != null && zone.getBranch() != null ? zone.getBranch().getBranchName() : null)
                .createdAt(area.getCreatedAt())
                .createdBy(area.getCreatedBy())
                .updatedAt(area.getUpdatedAt())
                .updatedBy(area.getUpdatedBy())
                .build();
    }
}
