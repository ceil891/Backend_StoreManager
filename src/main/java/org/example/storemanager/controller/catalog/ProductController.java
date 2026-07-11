package org.example.storemanager.controller.catalog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.catalog.product.CreateProductRequest;
import org.example.storemanager.dto.request.catalog.product.UpdateProductRequest;
import org.example.storemanager.dto.response.catalog.product.CreateProductResponse;
import org.example.storemanager.dto.response.catalog.product.DeleteProductResponse;
import org.example.storemanager.dto.response.catalog.product.ProductResponse;
import org.example.storemanager.dto.response.catalog.product.UpdateProductResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.catalog.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:product:create')")
    public ResponseEntity<ApiResponse<CreateProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:product:update')")
    public ResponseEntity<ApiResponse<UpdateProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        UpdateProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật sản phẩm thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:product:update-status')")
    public ResponseEntity<ApiResponse<UpdateProductResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        UpdateProductResponse response = productService.updateStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:product:delete')")
    public ResponseEntity<ApiResponse<DeleteProductResponse>> deleteProduct(@PathVariable Long id) {
        DeleteProductResponse response = productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa sản phẩm thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:product:view')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:product:view')")
    public ResponseEntity<ApiResponse<?>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                productService.getProductsPaginated(search, categoryId, isActive, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                productService.getAllProducts(search, categoryId, isActive, sort, includeDeleted)));
        }
    }
}
