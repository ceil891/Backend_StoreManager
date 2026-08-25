package org.example.storemanager.modules.catalog.service;

import org.example.storemanager.modules.catalog.dto.request.product.CreateProductRequest;
import org.example.storemanager.modules.catalog.dto.request.product.UpdateProductRequest;
import org.example.storemanager.modules.catalog.dto.response.product.*;
import org.example.storemanager.modules.common.dto.response.PageResponse;

import java.util.List;

public interface ProductService {
    CreateProductResponse createProduct(CreateProductRequest request);
    UpdateProductResponse updateProduct(Long id, UpdateProductRequest request);
    DeleteProductResponse deleteProduct(Long id);
    UpdateProductResponse updateStatus(Long id, Boolean isActive);
    ProductResponse getProductById(Long id);
    List<MapProductResponse> getAllProducts(String search, Long categoryId, Boolean isActive, String sort, boolean includeDeleted);
    PageResponse<MapProductResponse> getProductsPaginated(String search, Long categoryId, Boolean isActive, int page, int size, String sort, boolean includeDeleted);
    BulkProductImportResponse bulkCreateProducts(List<CreateProductRequest> requests);
}

