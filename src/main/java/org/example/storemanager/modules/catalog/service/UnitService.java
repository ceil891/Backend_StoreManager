package org.example.storemanager.modules.catalog.service;

import org.example.storemanager.modules.catalog.dto.request.unit.CreateUnitRequest;
import org.example.storemanager.modules.catalog.dto.request.unit.UpdateUnitRequest;
import org.example.storemanager.modules.catalog.dto.response.unit.CreateUnitResponse;
import org.example.storemanager.modules.catalog.dto.response.unit.DeleteUnitResponse;
import org.example.storemanager.modules.catalog.dto.response.unit.UpdateUnitResponse;
import org.example.storemanager.modules.catalog.dto.response.unit.UnitResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;

import java.util.List;

public interface UnitService {

    CreateUnitResponse createUnit(CreateUnitRequest request);

    UpdateUnitResponse updateUnit(Long id, UpdateUnitRequest request);


    DeleteUnitResponse deleteUnit(Long id);

    UpdateUnitResponse updateStatus(Long id, Boolean isActive);

    UnitResponse getUnitById(Long id);


    List<UnitResponse> getAllUnits(String search, Boolean isActive, String sort, boolean includeDeleted);


    PageResponse<UnitResponse> getUnitsPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted);
}
