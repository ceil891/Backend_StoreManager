package org.example.storemanager.modules.catalog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.catalog.dto.request.variant.CreateSingleVariantRequest;
import org.example.storemanager.modules.catalog.dto.request.variant.CreateVariantRequest;
import org.example.storemanager.modules.catalog.dto.request.variant.UpdateVariantRequest;
import org.example.storemanager.modules.catalog.dto.response.variant.CreateVariantResponse;
import org.example.storemanager.modules.catalog.dto.response.variant.VariantResponse;
import org.example.storemanager.modules.catalog.dto.response.pricelist.ActualPriceResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.catalog.service.ProductVariantService;
import org.example.storemanager.modules.catalog.service.PriceListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductVariantController {

    private final ProductVariantService productVariantService;
    private final PriceListService priceListService;

    // --- Search / Filter / List Variants ---
    @GetMapping("/variants")
    public ResponseEntity<ApiResponse<List<VariantResponse>>> getVariants(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String status) {
        if (sku != null) {
            try {
                return ResponseEntity.ok(ApiResponse.ok(List.of(productVariantService.getBySku(sku))));
            } catch (Exception e) {
                return ResponseEntity.ok(ApiResponse.ok(List.of()));
            }
        }
        if (barcode != null) {
            try {
                return ResponseEntity.ok(ApiResponse.ok(List.of(productVariantService.getByBarcode(barcode))));
            } catch (Exception e) {
                return ResponseEntity.ok(ApiResponse.ok(List.of()));
            }
        }
        if (productId != null) {
            return ResponseEntity.ok(ApiResponse.ok(productVariantService.getByProductId(productId)));
        }
        return ResponseEntity.ok(ApiResponse.ok(productVariantService.getAllVariants()));
    }

    @GetMapping("/variants/{id}")
    public ResponseEntity<ApiResponse<VariantResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productVariantService.getById(id)));
    }

    @GetMapping("/variants/sku/{sku}")
    public ResponseEntity<ApiResponse<VariantResponse>> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(ApiResponse.ok(productVariantService.getBySku(sku)));
    }

    @GetMapping("/variants/barcode/{barcode}")
    public ResponseEntity<ApiResponse<VariantResponse>> getByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(ApiResponse.ok(productVariantService.getByBarcode(barcode)));
    }

    @PostMapping("/products/{productId}/variants")
    public ResponseEntity<ApiResponse<VariantResponse>> createSingleVariant(
            @PathVariable Long productId,
            @Valid @RequestBody CreateSingleVariantRequest request) {
        VariantResponse response = productVariantService.createSingleVariant(productId, request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/variants/{id}")
    public ResponseEntity<ApiResponse<VariantResponse>> updateVariant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVariantRequest request) {
        VariantResponse response = productVariantService.updateVariant(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật biến thể thành công", response));
    }

    @DeleteMapping("/variants/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable Long id) {
        productVariantService.deleteVariant(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa biến thể thành công", null));
    }

    // --- Price Resolution ---
    @GetMapping("/variants/{variantId}/actual-price")
    public ResponseEntity<ApiResponse<ActualPriceResponse>> getActualPrice(
            @PathVariable Long variantId,
            @RequestParam Long branchId) {
        return ResponseEntity.ok(ApiResponse.ok(priceListService.resolveActualPrice(variantId, branchId)));
    }

    @GetMapping("/pricing/resolve")
    public ResponseEntity<ApiResponse<ActualPriceResponse>> resolvePrice(
            @RequestParam Long variantId,
            @RequestParam Long branchId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer quantity) {
        return ResponseEntity.ok(ApiResponse.ok(priceListService.resolveActualPrice(variantId, branchId)));
    }
}
