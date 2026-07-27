package org.example.storemanager.modules.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.inventory.dto.ImportReceiptDTO;
import org.example.storemanager.modules.catalog.dto.request.inventory.ImportCancelRequest;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/inventory", "/api/v1/inventories"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class InventoryImportApiController {

    private final InventoryService inventoryService;

    @GetMapping("/imports")
    public ResponseEntity<ApiResponse<List<ImportReceiptDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllImportReceipts()));
    }

    @GetMapping("/imports/{id}")
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getImportReceiptById(id)));
    }

    @PostMapping("/imports")
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> create(@RequestBody ImportReceiptDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(inventoryService.createImportReceipt(dto)));
    }

    @PutMapping("/imports/{id}")
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> update(@PathVariable Long id, @RequestBody ImportReceiptDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.updateImportReceipt(id, dto)));
    }

    @DeleteMapping("/imports/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        inventoryService.deleteImportReceipt(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/imports/{id}/submit")
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.submitImportReceipt(id)));
    }

    @PostMapping("/imports/{id}/approve")
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.approveImportReceipt(id)));
    }

    @PostMapping({"/imports/{id}/complete", "/imports/{id}/receive"})
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> receive(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.completeImportReceipt(id)));
    }

    @RequestMapping(value = "/imports/{id}/cancel", method = {RequestMethod.POST, RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) ImportCancelRequest request) {
        ImportCancelRequest req = request != null ? request : new ImportCancelRequest();
        if (req.getCancelReason() == null) {
            req.setCancelReason("Hủy phiếu nhập hàng");
        }
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.cancelImportReceipt(id, req)));
    }
}
