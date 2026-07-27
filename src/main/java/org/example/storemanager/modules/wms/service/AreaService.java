package org.example.storemanager.modules.wms.service;

import org.example.storemanager.modules.wms.dto.AreaDTO;
import java.util.List;

public interface AreaService {
    List<AreaDTO.Response> getAllAreas();
    List<AreaDTO.Response> getAreasByZoneId(Long zoneId);
    List<AreaDTO.Response> getAreasByBranchId(Long branchId);
    AreaDTO.Response getAreaById(Long id);
    AreaDTO.Response createArea(AreaDTO.Request request);
    AreaDTO.Response updateArea(Long id, AreaDTO.Request request);
    void deleteArea(Long id);
}
