package org.example.storemanager.service.catalog;

import org.example.storemanager.dto.request.catalog.unit.CreateUnitRequest;
import org.example.storemanager.dto.request.catalog.unit.UpdateUnitRequest;
import org.example.storemanager.dto.response.catalog.unit.CreateUnitResponse;
import org.example.storemanager.dto.response.catalog.unit.DeleteUnitResponse;
import org.example.storemanager.dto.response.catalog.unit.UpdateUnitResponse;
import org.example.storemanager.dto.response.catalog.unit.UnitResponse;
import org.example.storemanager.dto.response.common.PageResponse;

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
