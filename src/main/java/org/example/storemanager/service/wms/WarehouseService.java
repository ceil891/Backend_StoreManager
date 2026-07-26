package org.example.storemanager.service.wms;

import org.example.storemanager.dto.wms.WarehouseBinDTO;
import org.example.storemanager.dto.wms.WarehouseZoneDTO;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.entity.wms.WarehouseZone;

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
