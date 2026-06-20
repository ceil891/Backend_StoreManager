package org.example.storemanager.service.catalog;

import org.example.storemanager.dto.request.catalog.categories.CreateCategoriesRequest;
import org.example.storemanager.dto.request.catalog.categories.UpdateCategoriesRequest;
import org.example.storemanager.dto.response.catalog.categories.*;
import org.example.storemanager.dto.response.common.PageResponse;

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
