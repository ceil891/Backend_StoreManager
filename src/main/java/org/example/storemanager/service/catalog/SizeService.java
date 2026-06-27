package org.example.storemanager.service.catalog;

import org.example.storemanager.dto.request.catalog.size.CreateSizeRequest;
import org.example.storemanager.dto.request.catalog.size.UpdateSizeRequest;
import org.example.storemanager.dto.response.catalog.size.*;
import org.example.storemanager.dto.response.common.PageResponse;

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
