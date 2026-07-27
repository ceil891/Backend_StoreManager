package org.example.storemanager.modules.wms.service;

import org.example.storemanager.modules.wms.dto.RackDTO;
import java.util.List;

public interface RackService {
    List<RackDTO.Response> getAllRacks();
    List<RackDTO.Response> getRacksByAreaId(Long areaId);
    List<RackDTO.Response> getRacksByBranchId(Long branchId);
    RackDTO.Response getRackById(Long id);
    RackDTO.Response createRack(RackDTO.Request request);
    RackDTO.Response updateRack(Long id, RackDTO.Request request);
    void deleteRack(Long id);
}
