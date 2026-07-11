package org.example.storemanager.service.wms;

import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.entity.wms.WarehouseZone;

public interface WarehouseZoneService {
    WarehouseZone getOrCreateDefaultZone(Branch branch);
    WarehouseZone getOrCreateDefaultZone(Long branchId);
}
