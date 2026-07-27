package org.example.storemanager.modules.catalog.controller;

import jakarta.validation.Valid;
import org.example.storemanager.modules.catalog.dto.request.categories.CreateCategoriesRequest;
import org.example.storemanager.modules.catalog.dto.request.categories.UpdateCategoriesRequest;
import org.example.storemanager.modules.catalog.dto.response.categories.*;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.catalog.service.CategoriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoriesController {

    private final CategoriesService categoriesService;

    @Autowired
    public CategoriesController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    // ========== TẠO MỚI ==========
    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:category:create')")
    public ResponseEntity<ApiResponse<CreateCategoriesResponse>> create(@Valid @RequestBody CreateCategoriesRequest request) {
        CreateCategoriesResponse response = categoriesService.create(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    // ========== CẬP NHẬT ==========
    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:category:update')")
    public ResponseEntity<ApiResponse<UpdateCategoriesResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoriesRequest request) {
        UpdateCategoriesResponse response = categoriesService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật danh mục thành công", response));
    }

    // ========== BẬT/TẮT TRẠNG THÁI ==========
    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:category:update-status')")
    public ResponseEntity<ApiResponse<UpdateCategoriesResponse>> toggleStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        UpdateCategoriesResponse response = categoriesService.toggleStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", response));
    }

    // ========== XÓA MỀM (Phải tắt hoạt động trước) ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:category:delete')")
    public ResponseEntity<ApiResponse<DeleteCategoriesResponse>> softDelete(@PathVariable Long id) {
        DeleteCategoriesResponse response = categoriesService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa danh mục thành công", response));
    }

    // ========== KHÔI PHỤC ==========
    @PutMapping("/{id}/restore")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:category:restore')")
    public ResponseEntity<ApiResponse<UpdateCategoriesResponse>> restore(@PathVariable Long id) {
        UpdateCategoriesResponse response = categoriesService.restore(id);
        return ResponseEntity.ok(ApiResponse.ok("Khôi phục danh mục thành công", response));
    }

    // ========== XEM CHI TIẾT ==========
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:category:view')")
    public ResponseEntity<ApiResponse<CategoriesResponse>> getById(@PathVariable Long id) {
        CategoriesResponse response = categoriesService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH (phân trang hoặc tất cả) ==========
    /**
     * Tham số:
     * - search        : tìm kiếm theo mã/tên/mô tả
     * - isActive      : true=Hoạt động | false=Tắt | null=Tất cả
     * - includeDeleted: true=Bao gồm đã xóa | false (mặc định)=Chỉ chưa xóa
     * - page, size    : phân trang (nếu có cả 2 thì trả PageResponse, không thì trả List)
     * - sort          : ví dụ "categoryName,asc" hoặc "createdAt,desc"
     *
     * Ví dụ Postman:
     * GET /api/v1/categories?includeDeleted=true
     * GET /api/v1/categories?isActive=false&includeDeleted=false
     * GET /api/v1/categories?page=0&size=10&includeDeleted=true
     */
    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:category:view')")
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "categoryName,asc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                categoriesService.getCategoriesPaginated(search, isActive, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                categoriesService.getAllCategories(search, isActive, sort, includeDeleted)));
        }
    }

    // ========== CÂY DANH MỤC ==========
    @GetMapping("/tree")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:category:view')")
    public ResponseEntity<ApiResponse<List<MapCategoriesResponse>>> getTree() {
        List<MapCategoriesResponse> tree = categoriesService.getTree();
        return ResponseEntity.ok(ApiResponse.ok(tree));
    }

    // ========== DANH MỤC CON ==========
    @GetMapping("/{id}/children")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:category:view')")
    public ResponseEntity<ApiResponse<List<MapCategoriesResponse>>> getChildren(@PathVariable Long id) {
        List<MapCategoriesResponse> children = categoriesService.getChildren(id);
        return ResponseEntity.ok(ApiResponse.ok(children));
    }

    // ========== DANH MỤC CHA ==========
    @GetMapping("/{id}/parent")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:category:view')")
    public ResponseEntity<ApiResponse<CategoriesResponse>> getParent(@PathVariable Long id) {
        CategoriesResponse parent = categoriesService.getParent(id);
        return ResponseEntity.ok(ApiResponse.ok(parent));
    }
}
