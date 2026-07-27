package org.example.storemanager.modules.wms.service;

import org.example.storemanager.modules.wms.dto.WarehouseBinDTO;
import org.example.storemanager.modules.wms.dto.WarehouseZoneDTO;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.wms.entity.WarehouseZone;

import java.util.List;

public interface WarehouseService {
    // Zone CRUD
    List<WarehouseZoneDTO> getAllZones();
    WarehouseZoneDTO getZoneById(Long id);
    WarehouseZoneDTO createZone(WarehouseZoneDTO dto);
    WarehouseZoneDTO updateZone(Long id, WarehouseZoneDTO dto);
    void deleteZone(Long id);
    WarehouseZone getOrCreateDefaultZone(Branch branch);
    WarehouseZone getOrCreateDefaultZone(Long branchId);

    // Bin CRUD
    List<WarehouseBinDTO> getAllBins();
    List<WarehouseBinDTO> getBinsByZoneId(Long zoneId);
    List<WarehouseBinDTO> getBinsByRackId(Long rackId);
    WarehouseBinDTO getBinById(Long id);
    WarehouseBinDTO createBin(WarehouseBinDTO dto);
    WarehouseBinDTO updateBin(Long id, WarehouseBinDTO dto);
    void deleteBin(Long id);
}
