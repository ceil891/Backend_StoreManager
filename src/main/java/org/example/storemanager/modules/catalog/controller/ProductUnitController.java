package org.example.storemanager.modules.catalog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.catalog.dto.request.productunit.CreateProductUnitRequest;
import org.example.storemanager.modules.catalog.dto.request.productunit.UpdateProductUnitRequest;
import org.example.storemanager.modules.catalog.dto.response.productunit.ProductUnitResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.catalog.service.ProductUnitService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/units")
@RequiredArgsConstructor
public class ProductUnitController {

    private final ProductUnitService productUnitService;

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:productunit:view')")
    public ResponseEntity<ApiResponse<List<ProductUnitResponse>>> getProductUnits(@PathVariable Long productId) {
        List<ProductUnitResponse> responses = productUnitService.getProductUnits(productId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:productunit:create')")
    public ResponseEntity<ApiResponse<ProductUnitResponse>> createProductUnit(
            @PathVariable Long productId,
            @Valid @RequestBody CreateProductUnitRequest request) {
        ProductUnitResponse response = productUnitService.createProductUnit(productId, request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:productunit:update')")
    public ResponseEntity<ApiResponse<ProductUnitResponse>> updateProductUnit(
            @PathVariable Long productId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductUnitRequest request) {
        ProductUnitResponse response = productUnitService.updateProductUnit(productId, id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật đơn vị quy đổi thành công", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:productunit:update')")
    public ResponseEntity<ApiResponse<ProductUnitResponse>> updateStatus(
            @PathVariable Long productId,
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        ProductUnitResponse response = productUnitService.updateStatus(productId, id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái đơn vị quy đổi thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:productunit:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteProductUnit(
            @PathVariable Long productId,
            @PathVariable Long id) {
        productUnitService.deleteProductUnit(productId, id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa đơn vị quy đổi thành công", null));
    }
}
