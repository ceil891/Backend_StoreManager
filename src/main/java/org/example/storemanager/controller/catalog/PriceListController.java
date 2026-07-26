package org.example.storemanager.controller.catalog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.catalog.pricelist.CreatePriceListRequest;
import org.example.storemanager.dto.request.catalog.pricelist.PriceListDetailRequest;
import org.example.storemanager.dto.request.catalog.pricelist.UpdatePriceListRequest;
import org.example.storemanager.dto.response.catalog.pricelist.ActivePriceResponse;
import org.example.storemanager.dto.response.catalog.pricelist.PriceListDetailResponse;
import org.example.storemanager.dto.response.catalog.pricelist.PriceListResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.catalog.PriceListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PriceListController {

    private final PriceListService priceListService;

    @GetMapping("/price-lists")
    public ResponseEntity<ApiResponse<List<PriceListResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(priceListService.getAll()));
    }

    @GetMapping("/price-lists/{id}")
    public ResponseEntity<ApiResponse<PriceListResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(priceListService.getById(id)));
    }

    @PostMapping("/price-lists")
    public ResponseEntity<ApiResponse<PriceListResponse>> create(@Valid @RequestBody CreatePriceListRequest request) {
        PriceListResponse response = priceListService.create(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/price-lists/{id}")
    public ResponseEntity<ApiResponse<PriceListResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdatePriceListRequest request) {
        PriceListResponse response = priceListService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bảng giá thành công", response));
    }

    @DeleteMapping("/price-lists/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        priceListService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa bảng giá thành công", null));
    }

    // --- Price List Items ---

    @GetMapping("/price-lists/{priceListId}/items")
    public ResponseEntity<ApiResponse<List<PriceListDetailResponse>>> getItems(@PathVariable Long priceListId) {
        return ResponseEntity.ok(ApiResponse.ok(priceListService.getItems(priceListId)));
    }

    @PostMapping("/price-lists/{priceListId}/items")
    public ResponseEntity<ApiResponse<PriceListDetailResponse>> addItem(
            @PathVariable Long priceListId,
            @Valid @RequestBody PriceListDetailRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(priceListService.addItem(priceListId, request)));
    }

    @PutMapping("/price-list-items/{id}")
    public ResponseEntity<ApiResponse<PriceListDetailResponse>> updateItem(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, BigDecimal> body,
            @RequestParam(required = false) BigDecimal price) {
        BigDecimal finalPrice = price;
        if (finalPrice == null && body != null && body.containsKey("price")) {
            finalPrice = body.get("price");
        }
        if (finalPrice == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "price is required");
        }
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật giá thành phần thành công", priceListService.updateItem(id, finalPrice)));
    }

    @DeleteMapping("/price-list-items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        priceListService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa giá thành phần thành công", null));
    }
}
