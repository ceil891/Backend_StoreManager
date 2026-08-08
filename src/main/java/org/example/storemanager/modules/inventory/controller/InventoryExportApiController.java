package org.example.storemanager.modules.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.inventory.dto.StockOutDTO;
import org.example.storemanager.modules.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/inventory/exports", "/api/v1/inventories/exports", "/api/v1/inventories/stock-outs"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class InventoryExportApiController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockOutDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllStockOuts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockOutDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getStockOutById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockOutDTO>> create(@RequestBody StockOutDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(inventoryService.createStockOut(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StockOutDTO>> update(@PathVariable Long id, @RequestBody StockOutDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.updateStockOut(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        inventoryService.deleteStockOut(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
