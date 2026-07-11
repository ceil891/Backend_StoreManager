package org.example.storemanager.controller.catalog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.catalog.pricelist.CreatePriceListRequest;
import org.example.storemanager.dto.request.catalog.pricelist.UpdatePriceListRequest;
import org.example.storemanager.dto.response.catalog.pricelist.ActivePriceResponse;
import org.example.storemanager.dto.response.catalog.pricelist.PriceListResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.catalog.PriceListService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pricelists")
@RequiredArgsConstructor
public class PriceListController {

    private final PriceListService priceListService;

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:pricelist:view')")
    public ResponseEntity<ApiResponse<List<PriceListResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(priceListService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:pricelist:view')")
    public ResponseEntity<ApiResponse<PriceListResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(priceListService.getById(id)));
    }

    @GetMapping("/active")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:pricelist:view')")
    public ResponseEntity<ApiResponse<ActivePriceResponse>> resolveActivePrice(
            @RequestParam Long productId,
            @RequestParam(required = false) Long productUnitId,
            @RequestParam(required = false) Long branchId) {
        return ResponseEntity.ok(ApiResponse.ok(
                priceListService.resolveActivePrice(branchId, productId, productUnitId)));
    }

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:pricelist:create')")
    public ResponseEntity<ApiResponse<PriceListResponse>> create(
            @Valid @RequestBody CreatePriceListRequest request) {
        PriceListResponse response = priceListService.create(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:pricelist:update')")
    public ResponseEntity<ApiResponse<PriceListResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriceListRequest request) {
        PriceListResponse response = priceListService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bảng giá thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:pricelist:delete')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        priceListService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa bảng giá thành công", null));
    }
}
