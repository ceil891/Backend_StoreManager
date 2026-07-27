package org.example.storemanager.modules.catalog.service;

import org.example.storemanager.modules.catalog.dto.request.size.CreateSizeRequest;
import org.example.storemanager.modules.catalog.dto.request.size.UpdateSizeRequest;
import org.example.storemanager.modules.catalog.dto.response.size.*;
import org.example.storemanager.modules.common.dto.response.PageResponse;

import java.util.List;

public interface SizeService {
    CreateSizeResponse createSize(CreateSizeRequest request);

    UpdateSizeResponse updateSize(Long id, UpdateSizeRequest request);

    DeleteSizeResponse deleteSize(Long id);

    UpdateSizeResponse updateStatus(Long id, Boolean isActive);

    SizeResponse getSizeById(Long id);

    List<MapSizeResponse> getAllSizes(
            String search,
            Boolean isActive,
            String sort,
            boolean includeDeleted);

    PageResponse<MapSizeResponse> getSizesPaginated(
            String search,
            Boolean isActive,
            int page,
            int size,
            String sort,
            boolean includeDeleted);
}
