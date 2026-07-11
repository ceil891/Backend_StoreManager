package org.example.storemanager.controller.catalog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.catalog.combo.ComboDeductStockRequest;
import org.example.storemanager.dto.request.catalog.combo.CreateComboRequest;
import org.example.storemanager.dto.request.catalog.combo.UpdateComboRequest;
import org.example.storemanager.dto.response.catalog.combo.ComboResponse;
import org.example.storemanager.dto.response.catalog.combo.ComboSaveResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.service.catalog.ComboService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/combos")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:view')")
    public ResponseEntity<ApiResponse<PageResponse<ComboResponse>>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(comboService.search(search, isActive, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:view')")
    public ResponseEntity<ApiResponse<ComboResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(comboService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:create')")
    public ResponseEntity<ApiResponse<ComboSaveResponse>> create(
            @Valid @RequestBody CreateComboRequest request) {
        ComboSaveResponse response = comboService.create(request);
        String message = response.getWarnings() != null && !response.getWarnings().isEmpty()
                ? response.getWarnings().get(0)
                : "Tạo combo thành công";
        return ResponseEntity.status(201).body(ApiResponse.created(message, response));
    }

    @PutMapping("/{id}")
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

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:delete')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        comboService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa combo thành công", null));
    }

    @PostMapping("/{id}/deduct-stock")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:combo:deduct')")
    public ResponseEntity<ApiResponse<Void>> deductStock(
            @PathVariable Long id,
            @Valid @RequestBody ComboDeductStockRequest request) {
        comboService.deductDynamicComboStock(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Trừ tồn kho combo thành công", null));
    }
}
