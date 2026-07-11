package org.example.storemanager.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.catalog.inventory.InventoryAdjustRequest;
import org.example.storemanager.dto.request.catalog.inventory.SearchInventoryRequest;
import org.example.storemanager.dto.response.catalog.inventory.AdjustmentResponse;
import org.example.storemanager.dto.response.catalog.inventory.InventoryResponse;
import org.example.storemanager.dto.response.catalog.inventory.LowStockResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.service.inventory.InventoryService;
import org.example.storemanager.config.LogActivity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/search")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:inventory:search')")
    public ResponseEntity<ApiResponse<PageResponse<InventoryResponse>>> searchInventories(
            SearchInventoryRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<InventoryResponse> response = inventoryService.searchInventories(request, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:inventory:low-stock')")
    public ResponseEntity<ApiResponse<java.util.List<LowStockResponse>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getLowStock()));
    }

    @PostMapping("/adjust")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:inventory:adjust')")
    @LogActivity(actionType = "ADJUST", entityName = "SizeInventory", entityClass = org.example.storemanager.entity.inventory.SizeInventory.class)
    public ResponseEntity<ApiResponse<AdjustmentResponse>> adjustStock(@RequestBody InventoryAdjustRequest request) {
        AdjustmentResponse response = inventoryService.adjustStock(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:inventory:view')")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(@PathVariable Long id) {
        InventoryResponse response = inventoryService.getInventory(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
