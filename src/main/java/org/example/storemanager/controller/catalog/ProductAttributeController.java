package org.example.storemanager.controller.catalog;

import jakarta.validation.Valid;
import org.example.storemanager.dto.request.catalog.attribute.CreateAttributeRequest;
import org.example.storemanager.dto.request.catalog.attribute.CreateAttributeValueRequest;
import org.example.storemanager.dto.request.catalog.attribute.UpdateAttributeRequest;
import org.example.storemanager.dto.request.catalog.attribute.UpdateAttributeValueRequest;
import org.example.storemanager.dto.response.catalog.attribute.AttributeResponse;
import org.example.storemanager.dto.response.catalog.attribute.AttributeValueResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.catalog.ProductAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attributes")
public class ProductAttributeController {

    private final ProductAttributeService productAttributeService;

    @Autowired
    public ProductAttributeController(ProductAttributeService productAttributeService) {
        this.productAttributeService = productAttributeService;
    }

    // ========== TẠO MỚI THUỘC TÍNH ==========
    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:attribute:create')")
    public ResponseEntity<ApiResponse<AttributeResponse>> createAttribute(
            @Valid @RequestBody CreateAttributeRequest request) {
        AttributeResponse response = productAttributeService.createAttribute(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    // ========== CẬP NHẬT THUỘC TÍNH ==========
    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:attribute:update')")
    public ResponseEntity<ApiResponse<AttributeResponse>> updateAttribute(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttributeRequest request) {
        AttributeResponse response = productAttributeService.updateAttribute(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thuộc tính thành công", response));
    }

    // ========== CẬP NHẬT TRẠNG THÁI ==========
    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:attribute:update-status')")
    public ResponseEntity<ApiResponse<AttributeResponse>> toggleStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        AttributeResponse response = productAttributeService.toggleStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", response));
    }

    // ========== XÓA MỀM THUỘC TÍNH ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:attribute:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteAttribute(@PathVariable Long id) {
        productAttributeService.deleteAttribute(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thuộc tính thành công"));
    }

    // ========== XEM CHI TIẾT THEO ID ==========
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:attribute:view')")
    public ResponseEntity<ApiResponse<AttributeResponse>> getById(@PathVariable Long id) {
        AttributeResponse response = productAttributeService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH THUỘC TÍNH ==========
    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:attribute:view')")
    public ResponseEntity<ApiResponse<List<AttributeResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        List<AttributeResponse> responses = productAttributeService.getAll(search, isActive, includeDeleted);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    // ========== TẠO MỚI GIÁ TRỊ THUỘC TÍNH ==========
    @PostMapping("/values")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:attribute:create')")
    public ResponseEntity<ApiResponse<AttributeValueResponse>> createAttributeValue(
            @Valid @RequestBody CreateAttributeValueRequest request) {
        AttributeValueResponse response = productAttributeService.createAttributeValue(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    // ========== CẬP NHẬT GIÁ TRỊ THUỘC TÍNH ==========
    @PutMapping("/values/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:attribute:update')")
    public ResponseEntity<ApiResponse<AttributeValueResponse>> updateAttributeValue(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttributeValueRequest request) {
        AttributeValueResponse response = productAttributeService.updateAttributeValue(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật giá trị thuộc tính thành công", response));
    }

    // ========== XÓA MỀM GIÁ TRỊ THUỘC TÍNH ==========
    @DeleteMapping("/values/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:attribute:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteAttributeValue(@PathVariable Long id) {
        productAttributeService.deleteAttributeValue(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa giá trị thuộc tính thành công"));
    }

    // ========== DANH SÁCH GIÁ TRỊ THEO THUỘC TÍNH ==========
    @GetMapping("/{attributeId}/values")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:attribute:view')")
    public ResponseEntity<ApiResponse<List<AttributeValueResponse>>> getValuesByAttributeId(
            @PathVariable Long attributeId) {
        List<AttributeValueResponse> responses = productAttributeService.getValuesByAttributeId(attributeId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
