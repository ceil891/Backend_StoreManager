package org.example.storemanager.modules.catalog.controller;

import jakarta.validation.Valid;
import org.example.storemanager.modules.catalog.dto.request.size.CreateSizeRequest;
import org.example.storemanager.modules.catalog.dto.request.size.UpdateSizeRequest;
import org.example.storemanager.modules.catalog.dto.response.size.CreateSizeResponse;
import org.example.storemanager.modules.catalog.dto.response.size.DeleteSizeResponse;
import org.example.storemanager.modules.catalog.dto.response.size.UpdateSizeResponse;
import org.example.storemanager.modules.catalog.dto.response.size.SizeResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.catalog.service.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sizes")
public class SizeController {

    private final SizeService sizeService;

    @Autowired
    public SizeController(SizeService sizeService) {
        this.sizeService = sizeService;
    }

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:size:create')")
    public ResponseEntity<ApiResponse<CreateSizeResponse>> createSize(@Valid @RequestBody CreateSizeRequest request) {
        CreateSizeResponse response = sizeService.createSize(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:size:update')")
    public ResponseEntity<ApiResponse<UpdateSizeResponse>> updateSize(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSizeRequest request) {
        UpdateSizeResponse response = sizeService.updateSize(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật kích thước thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:size:update-status')")
    public ResponseEntity<ApiResponse<UpdateSizeResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        UpdateSizeResponse response = sizeService.updateStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:size:delete')")
    public ResponseEntity<ApiResponse<DeleteSizeResponse>> deleteSize(@PathVariable Long id) {
        DeleteSizeResponse response = sizeService.deleteSize(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa kích thước thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:size:view')")
    public ResponseEntity<ApiResponse<SizeResponse>> getSizeById(@PathVariable Long id) {
        SizeResponse response = sizeService.getSizeById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:size:view')")
    public ResponseEntity<ApiResponse<?>> getSizes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "sizeName,asc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                sizeService.getSizesPaginated(search, isActive, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                sizeService.getAllSizes(search, isActive, sort, includeDeleted)));
        }
    }
}
