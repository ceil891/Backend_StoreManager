package org.example.storemanager.service.catalog;

import org.example.storemanager.dto.request.catalog.product.CreateProductRequest;
import org.example.storemanager.dto.request.catalog.product.UpdateProductRequest;
import org.example.storemanager.dto.response.catalog.product.*;
import org.example.storemanager.dto.response.common.PageResponse;

import java.util.List;

public interface ProductService {
    CreateProductResponse createProduct(CreateProductRequest request);
    UpdateProductResponse updateProduct(Long id, UpdateProductRequest request);
    DeleteProductResponse deleteProduct(Long id);
    UpdateProductResponse updateStatus(Long id, Boolean isActive);
    ProductResponse getProductById(Long id);
    List<MapProductResponse> getAllProducts(String search, Long categoryId, Boolean isActive, String sort, boolean includeDeleted);
    PageResponse<MapProductResponse> getProductsPaginated(String search, Long categoryId, Boolean isActive, int page, int size, String sort, boolean includeDeleted);
}
