package org.example.storemanager.modules.catalog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.catalog.dto.request.combo.ComboDeductStockRequest;
import org.example.storemanager.modules.catalog.dto.request.combo.ComboDetailRequest;
import org.example.storemanager.modules.catalog.dto.request.combo.CreateComboRequest;
import org.example.storemanager.modules.catalog.dto.request.combo.UpdateComboRequest;
import org.example.storemanager.modules.catalog.dto.response.combo.ComboDetailResponse;
import org.example.storemanager.modules.catalog.dto.response.combo.ComboResponse;
import org.example.storemanager.modules.catalog.dto.response.combo.ComboSaveResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.catalog.service.ComboService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    @GetMapping("/combos")
    public ResponseEntity<ApiResponse<PageResponse<ComboResponse>>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(comboService.search(search, isActive, pageable)));
    }

    @GetMapping("/combos/{id}")
    public ResponseEntity<ApiResponse<ComboResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(comboService.getById(id)));
    }

    @PostMapping("/combos")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:create')")
    public ResponseEntity<ApiResponse<ComboSaveResponse>> create(
            @Valid @RequestBody CreateComboRequest request) {
        ComboSaveResponse response = comboService.create(request);
        String message = response.getWarnings() != null && !response.getWarnings().isEmpty()
                ? response.getWarnings().get(0)
                : "Tạo combo thành công";
        return ResponseEntity.status(201).body(ApiResponse.created(message, response));
    }

    @PutMapping("/combos/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:update')")
    public ResponseEntity<ApiResponse<ComboSaveResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComboRequest request) {
        ComboSaveResponse response = comboService.update(id, request);
        String message = response.getWarnings() != null && !response.getWarnings().isEmpty()
                ? response.getWarnings().get(0)
                : "Cập nhật combo thành công";
        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    @DeleteMapping("/combos/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:delete')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        comboService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa combo thành công", null));
    }

    @PostMapping("/combos/{id}/deduct-stock")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:deduct')")
    public ResponseEntity<ApiResponse<Void>> deductStock(
            @PathVariable Long id,
            @Valid @RequestBody ComboDeductStockRequest request) {
        comboService.deductDynamicComboStock(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Trừ tồn kho combo thành công", null));
    }

    // --- Combo Items ---

    @GetMapping("/combos/{comboId}/items")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:view')")
    public ResponseEntity<ApiResponse<List<ComboDetailResponse>>> getItems(@PathVariable Long comboId) {
        return ResponseEntity.ok(ApiResponse.ok(comboService.getItems(comboId)));
    }

    @PostMapping("/combos/{comboId}/items")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:update')")
    public ResponseEntity<ApiResponse<ComboDetailResponse>> addItem(
            @PathVariable Long comboId,
            @Valid @RequestBody ComboDetailRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(comboService.addItem(comboId, request)));
    }

    @PutMapping("/combo-items/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:update')")
    public ResponseEntity<ApiResponse<ComboDetailResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ComboDetailRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành phần combo thành công", comboService.updateItem(id, request)));
    }

    @DeleteMapping("/combo-items/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:update')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        comboService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thành phần combo thành công", null));
    }
}
