package org.example.storemanager.controller.catalog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.catalog.variant.CreateVariantRequest;
import org.example.storemanager.dto.request.catalog.variant.UpdateVariantRequest;
import org.example.storemanager.dto.response.catalog.variant.CreateVariantResponse;
import org.example.storemanager.dto.response.catalog.variant.VariantResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.catalog.ProductVariantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/variants
    // Tạo biến thể (1 hoặc nhiều tổ hợp). Xem mô tả DTO để biết body structure.
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:variant:create')")
    public ResponseEntity<ApiResponse<List<CreateVariantResponse>>> createVariants(
            @Valid @RequestBody CreateVariantRequest request) {
        List<CreateVariantResponse> responses = productVariantService.createVariants(request);
        return ResponseEntity.status(201).body(ApiResponse.created(responses));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/variants/{id}
    // Cập nhật barcode / ảnh / giá override. SKU và variantCode là immutable.
    // ──────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:variant:update')")
    public ResponseEntity<ApiResponse<VariantResponse>> updateVariant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVariantRequest request) {
        VariantResponse response = productVariantService.updateVariant(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật biến thể thành công", response));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/variants/{id}/status?isActive=true|false
    // Bật/tắt hoạt động biến thể.
    // ──────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:variant:update-status')")
    public ResponseEntity<ApiResponse<VariantResponse>> toggleStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        VariantResponse response = productVariantService.toggleStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái biến thể thành công", response));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/variants/{id}
    // Xóa mềm (chỉ khi đã tắt hoạt động).
    // ──────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:variant:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable Long id) {
        productVariantService.deleteVariant(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa biến thể thành công", null));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/variants/{id}
    // Xem chi tiết 1 biến thể (kèm danh sách thuộc tính Size/Color...).
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:variant:view')")
    public ResponseEntity<ApiResponse<VariantResponse>> getById(@PathVariable Long id) {
        VariantResponse response = productVariantService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/variants?productId=1
    // Lấy toàn bộ biến thể của 1 sản phẩm.
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:variant:view')")
    public ResponseEntity<ApiResponse<List<VariantResponse>>> getByProductId(
            @RequestParam Long productId) {
        List<VariantResponse> responses = productVariantService.getByProductId(productId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
