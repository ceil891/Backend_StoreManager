package org.example.storemanager.modules.catalog.service;

import org.example.storemanager.modules.catalog.dto.request.categories.CreateCategoriesRequest;
import org.example.storemanager.modules.catalog.dto.request.categories.UpdateCategoriesRequest;
import org.example.storemanager.modules.catalog.dto.response.categories.*;
import org.example.storemanager.modules.common.dto.response.PageResponse;

import java.util.List;

public interface CategoriesService {
    CreateCategoriesResponse create(CreateCategoriesRequest request);

    UpdateCategoriesResponse update(Long id, UpdateCategoriesRequest request);

    DeleteCategoriesResponse softDelete(Long id);

    UpdateCategoriesResponse toggleStatus(Long id, Boolean isActive);

    UpdateCategoriesResponse restore(Long id);

    CategoriesResponse getById(Long id);

    List<MapCategoriesResponse> getAllCategories(String search, Boolean isActive, String sort, boolean includeDeleted);

    PageResponse<MapCategoriesResponse> getCategoriesPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted);

    List<MapCategoriesResponse> getTree();

    List<MapCategoriesResponse> getChildren(Long id);

    CategoriesResponse getParent(Long id);
}
