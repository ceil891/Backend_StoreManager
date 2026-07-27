package org.example.storemanager.modules.wms.controller;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.wms.entity.ProductLocation;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.wms.repository.ProductLocationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.wms.entity.WarehouseBin;
import org.example.storemanager.modules.wms.entity.ProductLocation;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.wms.repository.WarehouseBinRepository;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/wms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductLocationApiController {

    private final ProductLocationRepository productLocationRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final WarehouseBinRepository warehouseBinRepository;

    @GetMapping("/product-locations")
    public ResponseEntity<ApiResponse<List<ProductLocationResponse>>> getAllProductLocations(
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long binId) {
        
        List<ProductLocation> sourceList;
        if (binId != null) {
            sourceList = productLocationRepository.findByBinIdAndIsDeletedFalse(binId);
        } else if (variantId != null) {
            ProductVariant variant = productVariantRepository.findByIdAndIsDeletedFalse(variantId).orElse(null);
            if (variant != null) {
                sourceList = productLocationRepository.findByProductIdAndIsDeletedFalse(variant.getProduct().getId());
            } else {
                sourceList = List.of();
            }
        } else {
            sourceList = productLocationRepository.findAll().stream().filter(pl -> !Boolean.TRUE.equals(pl.getIsDeleted())).toList();
        }

        List<ProductLocationResponse> list = sourceList.stream()
                .map(pl -> ProductLocationResponse.builder()
                        .productId(pl.getProduct().getId())
                        .productCode(pl.getProduct().getProductCode())
                        .productName(pl.getProduct().getName())
                        .binId(pl.getBin().getId())
                        .binCode(pl.getBin().getBinCode())
                        .quantity(pl.getQuantity())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/variants/{variantId}/locations")
    public ResponseEntity<ApiResponse<List<ProductLocationResponse>>> getLocationsForVariant(@PathVariable Long variantId) {
        ProductVariant variant = productVariantRepository.findByIdAndIsDeletedFalse(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));

        List<ProductLocationResponse> list = productLocationRepository.findByProductIdAndIsDeletedFalse(variant.getProduct().getId())
                .stream()
                .map(pl -> ProductLocationResponse.builder()
                        .productId(pl.getProduct().getId())
                        .productCode(pl.getProduct().getProductCode())
                        .productName(pl.getProduct().getName())
                        .binId(pl.getBin().getId())
                        .binCode(pl.getBin().getBinCode())
                        .quantity(pl.getQuantity())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/bins/{binId}/products")
    public ResponseEntity<ApiResponse<List<ProductLocationResponse>>> getProductsInBin(@PathVariable Long binId) {
        List<ProductLocationResponse> list = productLocationRepository.findByBinIdAndIsDeletedFalse(binId)
                .stream()
                .map(pl -> ProductLocationResponse.builder()
                        .productId(pl.getProduct().getId())
                        .productCode(pl.getProduct().getProductCode())
                        .productName(pl.getProduct().getName())
                        .binId(pl.getBin().getId())
                        .binCode(pl.getBin().getBinCode())
                        .quantity(pl.getQuantity())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/product-locations/assign")
    public ResponseEntity<ApiResponse<ProductLocationResponse>> assignProductLocation(
            @jakarta.validation.Valid @RequestBody AssignRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        WarehouseBin bin = warehouseBinRepository.findById(request.getBinId())
                .filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseBin", "id", request.getBinId()));

        ProductLocation pl = productLocationRepository.findByProductIdAndBinIdAndIsDeletedFalse(
                request.getProductId(), request.getBinId()).orElse(null);

        if (pl == null) {
            pl = ProductLocation.builder()
                    .product(product)
                    .bin(bin)
                    .quantity(request.getQuantity())
                    .build();
        } else {
            pl.setQuantity(request.getQuantity());
        }

        ProductLocation saved = productLocationRepository.save(pl);

        ProductLocationResponse response = ProductLocationResponse.builder()
                .productId(saved.getProduct().getId())
                .productCode(saved.getProduct().getProductCode())
                .productName(saved.getProduct().getName())
                .binId(saved.getBin().getId())
                .binCode(saved.getBin().getBinCode())
                .quantity(saved.getQuantity())
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Gán vị trí sản phẩm thành công", response));
    }

    @Data
    public static class AssignRequest {
        @NotNull(message = "Product ID cannot be null")
        private Long productId;
        @NotNull(message = "Bin ID cannot be null")
        private Long binId;
        @NotNull(message = "Quantity cannot be null")
        @Min(value = 0, message = "Quantity cannot be negative")
        private BigDecimal quantity;
    }

    @Data
    @Builder
    public static class ProductLocationResponse {
        private Long productId;
        private String productCode;
        private String productName;
        private Long binId;
        private String binCode;
        private BigDecimal quantity;
    }
}
