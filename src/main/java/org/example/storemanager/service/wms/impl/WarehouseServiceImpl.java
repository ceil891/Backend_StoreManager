package org.example.storemanager.service.wms.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.wms.WarehouseBinDTO;
import org.example.storemanager.dto.wms.WarehouseZoneDTO;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.entity.wms.Area;
import org.example.storemanager.entity.wms.Rack;
import org.example.storemanager.entity.wms.WarehouseBin;
import org.example.storemanager.entity.wms.WarehouseZone;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.system.BranchRepository;
import org.example.storemanager.repository.wms.RackRepository;
import org.example.storemanager.repository.wms.WarehouseBinRepository;
import org.example.storemanager.repository.wms.WarehouseZoneRepository;
import org.example.storemanager.service.wms.WarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    public static final String DEFAULT_ZONE_CODE = "DEFAULT";
    public static final String DEFAULT_ZONE_NAME = "Kho tổng";

    private final WarehouseZoneRepository warehouseZoneRepository;
    private final WarehouseBinRepository warehouseBinRepository;
    private final RackRepository rackRepository;
    private final BranchRepository branchRepository;

    // --- Zone CRUD ---

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseZoneDTO> getAllZones() {
        return warehouseZoneRepository.findAll().stream()
                .filter(z -> !Boolean.TRUE.equals(z.getIsDeleted()))
                .map(this::toZoneDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseZoneDTO getZoneById(Long id) {
        WarehouseZone zone = warehouseZoneRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseZone", "id", id));
        return toZoneDTO(zone);
    }

    @Override
    public WarehouseZoneDTO createZone(WarehouseZoneDTO dto) {
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));

        WarehouseZone zone = WarehouseZone.builder()
                .zoneCode(dto.getZoneCode())
                .zoneName(dto.getZoneName())
                .conditions(dto.getConditions())
                .capacity(dto.getCapacity())
                .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
                .description(dto.getDescription())
                .branch(branch)
                .build();
        zone.setIsDeleted(false);

        WarehouseZone saved = warehouseZoneRepository.save(zone);
        return toZoneDTO(saved);
    }

    @Override
    public WarehouseZoneDTO updateZone(Long id, WarehouseZoneDTO dto) {
        WarehouseZone zone = warehouseZoneRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseZone", "id", id));

        if (dto.getBranchId() != null) {
            Branch branch = branchRepository.findByIdAndIsDeletedFalse(dto.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
            zone.setBranch(branch);
        }

        zone.setZoneCode(dto.getZoneCode());
        zone.setZoneName(dto.getZoneName());
        zone.setConditions(dto.getConditions());
        if (dto.getCapacity() != null) zone.setCapacity(dto.getCapacity());
        if (dto.getStatus() != null) zone.setStatus(dto.getStatus());
        if (dto.getDescription() != null) zone.setDescription(dto.getDescription());

        WarehouseZone saved = warehouseZoneRepository.save(zone);
        return toZoneDTO(saved);
    }

    @Override
    public void deleteZone(Long id) {
        WarehouseZone zone = warehouseZoneRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseZone", "id", id));
        zone.setIsDeleted(true);
        warehouseZoneRepository.save(zone);
    }

    @Override
    public WarehouseZone getOrCreateDefaultZone(Branch branch) {
        return warehouseZoneRepository.findByBranchIdAndZoneCodeAndIsDeletedFalse(branch.getId(), DEFAULT_ZONE_CODE)
                .orElseGet(() -> {
                    WarehouseZone zone = WarehouseZone.builder()
                            .zoneCode(DEFAULT_ZONE_CODE)
                            .zoneName(DEFAULT_ZONE_NAME)
                            .status("ACTIVE")
                            .branch(branch)
                            .build();
                    zone.setIsDeleted(false);
                    return warehouseZoneRepository.save(zone);
                });
    }

    @Override
    public WarehouseZone getOrCreateDefaultZone(Long branchId) {
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));
        return getOrCreateDefaultZone(branch);
    }

    // --- Bin CRUD ---

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseBinDTO> getAllBins() {
        return warehouseBinRepository.findByIsDeletedFalse().stream()
                .map(this::toBinDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseBinDTO> getBinsByZoneId(Long zoneId) {
        return warehouseBinRepository.findByZoneId(zoneId).stream()
                .map(this::toBinDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseBinDTO> getBinsByRackId(Long rackId) {
        return warehouseBinRepository.findByRack_IdAndIsDeletedFalse(rackId).stream()
                .map(this::toBinDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseBinDTO getBinById(Long id) {
        WarehouseBin bin = warehouseBinRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseBin", "id", id));
        return toBinDTO(bin);
    }

    @Override
    public WarehouseBinDTO createBin(WarehouseBinDTO dto) {
        // Bin giờ FK vào Rack (không phải Zone trực tiếp)
        Rack rack = rackRepository.findById(dto.getRackId())
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Rack", "id", dto.getRackId()));

        WarehouseBin bin = WarehouseBin.builder()
                .binCode(dto.getBinCode())
                .barcode(dto.getBarcode())
                .maxWeightKg(dto.getMaxWeightKg())
                .maxVolumeM3(dto.getMaxVolumeM3())
                .maxPallet(dto.getMaxPallet())
                .status(dto.getStatus() != null ? dto.getStatus() : "EMPTY")
                .description(dto.getDescription())
                .rack(rack)
                .build();
        bin.setIsDeleted(false);

        WarehouseBin saved = warehouseBinRepository.save(bin);
        return toBinDTO(saved);
    }

    @Override
    public WarehouseBinDTO updateBin(Long id, WarehouseBinDTO dto) {
        WarehouseBin bin = warehouseBinRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseBin", "id", id));

        if (dto.getRackId() != null) {
            Rack rack = rackRepository.findById(dto.getRackId())
                    .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                    .orElseThrow(() -> new ResourceNotFoundException("Rack", "id", dto.getRackId()));
            bin.setRack(rack);
        }

        bin.setBinCode(dto.getBinCode());
        bin.setBarcode(dto.getBarcode());
        if (dto.getMaxWeightKg() != null) bin.setMaxWeightKg(dto.getMaxWeightKg());
        if (dto.getMaxVolumeM3() != null) bin.setMaxVolumeM3(dto.getMaxVolumeM3());
        if (dto.getMaxPallet() != null) bin.setMaxPallet(dto.getMaxPallet());
        if (dto.getStatus() != null) bin.setStatus(dto.getStatus());
        if (dto.getDescription() != null) bin.setDescription(dto.getDescription());

        WarehouseBin saved = warehouseBinRepository.save(bin);
        return toBinDTO(saved);
    }

    @Override
    public void deleteBin(Long id) {
        WarehouseBin bin = warehouseBinRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseBin", "id", id));
        bin.setIsDeleted(true);
        warehouseBinRepository.save(bin);
    }

    // --- Mappings ---

    private WarehouseZoneDTO toZoneDTO(WarehouseZone zone) {
        return WarehouseZoneDTO.builder()
                .id(zone.getId())
                .zoneCode(zone.getZoneCode())
                .zoneName(zone.getZoneName())
                .conditions(zone.getConditions())
                .capacity(zone.getCapacity())
                .status(zone.getStatus())
                .description(zone.getDescription())
                .branchId(zone.getBranch() != null ? zone.getBranch().getId() : null)
                .branchName(zone.getBranch() != null ? zone.getBranch().getBranchName() : null)
                .createdAt(zone.getCreatedAt())
                .createdBy(zone.getCreatedBy())
                .updatedAt(zone.getUpdatedAt())
                .updatedBy(zone.getUpdatedBy())
                .build();
    }

    private WarehouseBinDTO toBinDTO(WarehouseBin bin) {
        Rack rack = bin.getRack();
        Area area = rack != null ? rack.getArea() : null;
        WarehouseZone zone = area != null ? area.getZone() : null;
        return WarehouseBinDTO.builder()
                .id(bin.getId())
                .binCode(bin.getBinCode())
                .barcode(bin.getBarcode())
                .maxWeightKg(bin.getMaxWeightKg())
                .maxVolumeM3(bin.getMaxVolumeM3())
                .maxPallet(bin.getMaxPallet())
                .status(bin.getStatus())
                .description(bin.getDescription())
                .rackId(rack != null ? rack.getId() : null)
                .rackCode(rack != null ? rack.getRackCode() : null)
                .rackName(rack != null ? rack.getRackName() : null)
                .areaId(area != null ? area.getId() : null)
                .areaCode(area != null ? area.getAreaCode() : null)
                .zoneId(zone != null ? zone.getId() : null)
                .zoneCode(zone != null ? zone.getZoneCode() : null)
                .branchId(zone != null && zone.getBranch() != null ? zone.getBranch().getId() : null)
                .branchName(zone != null && zone.getBranch() != null ? zone.getBranch().getBranchName() : null)
                .createdAt(bin.getCreatedAt())
                .createdBy(bin.getCreatedBy())
                .updatedAt(bin.getUpdatedAt())
                .updatedBy(bin.getUpdatedBy())
                .build();
    }
}
