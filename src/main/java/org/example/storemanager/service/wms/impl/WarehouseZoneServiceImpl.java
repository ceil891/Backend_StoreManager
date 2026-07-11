package org.example.storemanager.service.wms.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.entity.wms.WarehouseZone;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.system.BranchRepository;
import org.example.storemanager.repository.wms.WarehouseZoneRepository;
import org.example.storemanager.service.wms.WarehouseZoneService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseZoneServiceImpl implements WarehouseZoneService {

    public static final String DEFAULT_ZONE_CODE = "DEFAULT";
    public static final String DEFAULT_ZONE_NAME = "Kho tổng";

    private final WarehouseZoneRepository warehouseZoneRepository;
    private final BranchRepository branchRepository;

    @Override
    public WarehouseZone getOrCreateDefaultZone(Branch branch) {
        return warehouseZoneRepository.findByBranchIdAndZoneCodeAndIsDeletedFalse(branch.getId(), DEFAULT_ZONE_CODE)
                .orElseGet(() -> {
                    WarehouseZone zone = WarehouseZone.builder()
                            .zoneCode(DEFAULT_ZONE_CODE)
                            .zoneName(DEFAULT_ZONE_NAME)
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
}
