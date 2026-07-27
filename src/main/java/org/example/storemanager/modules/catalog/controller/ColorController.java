package org.example.storemanager.modules.catalog.controller;

import jakarta.validation.Valid;
import org.example.storemanager.modules.catalog.dto.request.color.CreateColorRequest;
import org.example.storemanager.modules.catalog.dto.request.color.UpdateColorRequest;
import org.example.storemanager.modules.catalog.dto.response.color.CreateColorResponse;
import org.example.storemanager.modules.catalog.dto.response.color.DeleteColorResponse;
import org.example.storemanager.modules.catalog.dto.response.color.UpdateColorResponse;
import org.example.storemanager.modules.catalog.dto.response.color.ColorResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.catalog.service.ColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/colors")
public class ColorController {

    private final ColorService colorService;

    @Autowired
    public ColorController(ColorService colorService) {
        this.colorService = colorService;
    }

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:color:create')")
    public ResponseEntity<ApiResponse<CreateColorResponse>> createColor(@Valid @RequestBody CreateColorRequest request) {
        CreateColorResponse response = colorService.createColor(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:color:update')")
    public ResponseEntity<ApiResponse<UpdateColorResponse>> updateColor(
            @PathVariable Long id,
            @Valid @RequestBody UpdateColorRequest request) {
        UpdateColorResponse response = colorService.updateColor(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật màu sắc thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:color:update-status')")
    public ResponseEntity<ApiResponse<UpdateColorResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        UpdateColorResponse response = colorService.updateStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:color:delete')")
    public ResponseEntity<ApiResponse<DeleteColorResponse>> deleteColor(@PathVariable Long id) {
        DeleteColorResponse response = colorService.deleteColor(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa màu sắc thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:color:view')")
    public ResponseEntity<ApiResponse<ColorResponse>> getColorById(@PathVariable Long id) {
        ColorResponse response = colorService.getColorById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:color:view')")
    public ResponseEntity<ApiResponse<?>> getColors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "colorName,asc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                colorService.getColorsPaginated(search, isActive, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                colorService.getAllColors(search, isActive, sort, includeDeleted)));
        }
    }
}
