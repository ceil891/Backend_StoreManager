package org.example.storemanager.modules.catalog.service;

import org.example.storemanager.modules.catalog.dto.request.productunit.CreateProductUnitRequest;
import org.example.storemanager.modules.catalog.dto.request.productunit.UpdateProductUnitRequest;
import org.example.storemanager.modules.catalog.dto.response.productunit.ProductUnitResponse;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.Unit;

import java.util.List;

public interface ProductUnitService {
    List<ProductUnitResponse> getProductUnits(Long productId);
    ProductUnitResponse createProductUnit(Long productId, CreateProductUnitRequest request);
    ProductUnitResponse updateProductUnit(Long productId, Long id, UpdateProductUnitRequest request);
    void deleteProductUnit(Long productId, Long id);
    ProductUnitResponse updateStatus(Long productId, Long id, Boolean isActive);

    void createBaseProductUnit(Product product, Unit baseUnit, String username);
    void syncBaseProductUnit(Product product, String username);
    void validateBarcode(String barcode, Long excludeProductUnitId, Long excludeProductId);
}
