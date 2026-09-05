package org.example.storemanager.modules.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.inventory.dto.InventoryCheckDTO;
import org.example.storemanager.modules.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/inventory/checks", "/api/v1/inventories/checks"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class InventoryCheckApiController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryCheckDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllInventoryChecks()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryCheckDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getInventoryCheckById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryCheckDTO>> create(@RequestBody InventoryCheckDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(inventoryService.createInventoryCheck(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryCheckDTO>> update(@PathVariable Long id, @RequestBody InventoryCheckDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.updateInventoryCheck(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        inventoryService.deleteInventoryCheck(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<InventoryCheckDTO>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.approveInventoryCheck(id)));
    }
}
