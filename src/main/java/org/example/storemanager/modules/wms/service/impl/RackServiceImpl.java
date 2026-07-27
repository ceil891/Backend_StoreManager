package org.example.storemanager.modules.wms.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.wms.dto.RackDTO;
import org.example.storemanager.modules.wms.entity.Area;
import org.example.storemanager.modules.wms.entity.Rack;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.wms.repository.AreaRepository;
import org.example.storemanager.modules.wms.repository.RackRepository;
import org.example.storemanager.modules.wms.service.RackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RackServiceImpl implements RackService {

    private final RackRepository rackRepository;
    private final AreaRepository areaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RackDTO.Response> getAllRacks() {
        return rackRepository.findAllActive().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RackDTO.Response> getRacksByAreaId(Long areaId) {
        return rackRepository.findByArea_IdAndIsDeletedFalse(areaId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RackDTO.Response> getRacksByBranchId(Long branchId) {
        return rackRepository.findActiveByBranch(branchId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RackDTO.Response getRackById(Long id) {
        Rack rack = rackRepository.findById(id)
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Rack", "id", id));
        return toResponse(rack);
    }

    @Override
    public RackDTO.Response createRack(RackDTO.Request request) {
        if (rackRepository.existsByRackCode(request.getRackCode())) {
            throw new DuplicateResourceException("Rack", "rackCode", request.getRackCode());
        }

        Area area = areaRepository.findById(request.getAreaId())
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", request.getAreaId()));

        Rack rack = Rack.builder()
                .rackCode(request.getRackCode())
                .rackName(request.getRackName())
                .maxWeightKg(request.getMaxWeightKg())
                .maxVolumeM3(request.getMaxVolumeM3())
                .maxPallet(request.getMaxPallet())
                .description(request.getDescription())
                .isActive(Boolean.TRUE.equals(request.getIsActive()))
                .area(area)
                .build();
        rack.setIsDeleted(false);

        return toResponse(rackRepository.save(rack));
    }

    @Override
    public RackDTO.Response updateRack(Long id, RackDTO.Request request) {
        Rack rack = rackRepository.findById(id)
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Rack", "id", id));

        if (request.getAreaId() != null) {
            Area area = areaRepository.findById(request.getAreaId())
                    .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                    .orElseThrow(() -> new ResourceNotFoundException("Area", "id", request.getAreaId()));
            rack.setArea(area);
        }

        rack.setRackCode(request.getRackCode());
        rack.setRackName(request.getRackName());
        rack.setMaxWeightKg(request.getMaxWeightKg());
        rack.setMaxVolumeM3(request.getMaxVolumeM3());
        rack.setMaxPallet(request.getMaxPallet());
        rack.setDescription(request.getDescription());
        if (request.getIsActive() != null) rack.setIsActive(request.getIsActive());

        return toResponse(rackRepository.save(rack));
    }

    @Override
    public void deleteRack(Long id) {
        Rack rack = rackRepository.findById(id)
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Rack", "id", id));
        rack.setIsDeleted(true);
        rackRepository.save(rack);
    }

    private RackDTO.Response toResponse(Rack rack) {
        Area area = rack.getArea();
        return RackDTO.Response.builder()
                .id(rack.getId())
                .rackCode(rack.getRackCode())
                .rackName(rack.getRackName())
                .maxWeightKg(rack.getMaxWeightKg())
                .maxVolumeM3(rack.getMaxVolumeM3())
                .maxPallet(rack.getMaxPallet())
                .description(rack.getDescription())
                .isActive(rack.getIsActive())
                .areaId(area != null ? area.getId() : null)
                .areaCode(area != null ? area.getAreaCode() : null)
                .areaName(area != null ? area.getAreaName() : null)
                .zoneId(area != null && area.getZone() != null ? area.getZone().getId() : null)
                .zoneCode(area != null && area.getZone() != null ? area.getZone().getZoneCode() : null)
                .branchId(area != null && area.getZone() != null && area.getZone().getBranch() != null
                        ? area.getZone().getBranch().getId() : null)
                .branchName(area != null && area.getZone() != null && area.getZone().getBranch() != null
                        ? area.getZone().getBranch().getBranchName() : null)
                .createdAt(rack.getCreatedAt())
                .createdBy(rack.getCreatedBy())
                .updatedAt(rack.getUpdatedAt())
                .updatedBy(rack.getUpdatedBy())
                .build();
    }
}
