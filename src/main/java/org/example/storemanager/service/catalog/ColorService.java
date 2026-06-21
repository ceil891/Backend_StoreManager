package org.example.storemanager.service.catalog;

import org.example.storemanager.dto.request.catalog.color.CreateColorRequest;
import org.example.storemanager.dto.request.catalog.color.UpdateColorRequest;
import org.example.storemanager.dto.response.catalog.color.*;
import org.example.storemanager.dto.response.common.PageResponse;

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
