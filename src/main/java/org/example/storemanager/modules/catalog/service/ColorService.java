package org.example.storemanager.modules.catalog.service;

import org.example.storemanager.modules.catalog.dto.request.color.CreateColorRequest;
import org.example.storemanager.modules.catalog.dto.request.color.UpdateColorRequest;
import org.example.storemanager.modules.catalog.dto.response.color.*;
import org.example.storemanager.modules.common.dto.response.PageResponse;

import java.util.List;

public interface ColorService {
    CreateColorResponse createColor(CreateColorRequest request);

    UpdateColorResponse updateColor(Long id, UpdateColorRequest request);

    DeleteColorResponse deleteColor(Long id);

    UpdateColorResponse updateStatus(Long id, Boolean isActive);

    ColorResponse getColorById(Long id);

    List<MapColorResponse> getAllColors(
            String search,
            Boolean isActive,
            String sort,
            boolean includeDeleted);

    PageResponse<MapColorResponse> getColorsPaginated(
            String search,
            Boolean isActive,
            int page,
            int size,
            String sort,
            boolean includeDeleted);
}
