package org.example.storemanager.modules.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.catalog.dto.request.inventory.InventoryAdjustRequest;
import org.example.storemanager.modules.catalog.dto.request.inventory.SearchInventoryRequest;
import org.example.storemanager.modules.catalog.dto.response.inventory.AdjustmentResponse;
import org.example.storemanager.modules.catalog.dto.response.inventory.InventoryResponse;
import org.example.storemanager.modules.catalog.dto.response.inventory.LowStockResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.inventory.service.InventoryService;
import org.example.storemanager.shared.annotation.BranchScoped;
import org.example.storemanager.shared.security.UserContextHolder;
import org.example.storemanager.shared.config.LogActivity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import org.example.storemanager.modules.catalog.dto.response.inventory.StockLedgerResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@BranchScoped
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllInventories(
            @RequestParam(required = false) Long branchId) {
        Long effectiveBranchId = UserContextHolder.getEffectiveBranchId(branchId);
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllInventories(effectiveBranchId)));
    }

    @GetMapping("/ledger")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:inventory:search')")
    public ResponseEntity<ApiResponse<List<StockLedgerResponse>>> getStockLedger(
            @RequestParam(required = false) Long branchId) {
        Long effectiveBranchId = UserContextHolder.getEffectiveBranchId(branchId);
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getStockLedger(effectiveBranchId)));
    }

    @GetMapping("/search")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:inventory:search')")
    public ResponseEntity<ApiResponse<PageResponse<InventoryResponse>>> searchInventories(
            SearchInventoryRequest request,
            Pageable pageable) {
        if (request != null) {
            request.setBranchId(UserContextHolder.getEffectiveBranchId(request.getBranchId()));
        }
        PageResponse<InventoryResponse> response = inventoryService.searchInventories(request, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:inventory:low-stock')")
    public ResponseEntity<ApiResponse<List<LowStockResponse>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getLowStock()));
    }

    @PostMapping("/adjust")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:inventory:adjust')")
    @LogActivity(actionType = "ADJUST", entityName = "SizeInventory", entityClass = org.example.storemanager.modules.inventory.entity.SizeInventory.class)
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

    // --- Stock Queries ---
    
    @GetMapping("/stock")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:inventory:search')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryStock() {
        // Alias for root GET
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllInventories()));
    }

    @GetMapping("/stock/by-bin")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:inventory:search')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryStockByBin() {
        // Same as default for now, could be grouped by bin
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllInventories()));
    }

    @GetMapping("/stock/history")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:inventory:search')")
    public ResponseEntity<ApiResponse<List<StockLedgerResponse>>> getInventoryStockHistory() {
        // Alias for /ledger
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getStockLedger()));
    }
}
