package org.example.storemanager.modules.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.inventory.dto.CancelIssueDTO;
import org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO;
import org.example.storemanager.modules.inventory.dto.StockTransferDTO;
import org.example.storemanager.modules.catalog.dto.request.inventory.*;
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
public class InventoryTransfersApiController {

    private final InventoryService inventoryService;

    // ==========================================
    // --- CHUYỂN KHO — INVENTORY TRANSFER ---
    // ==========================================

    @GetMapping("/transfers")
    public ResponseEntity<ApiResponse<List<StockTransferDTO>>> getAllTransfers() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllStockTransfers()));
    }

    @GetMapping("/transfers/{id}")
    public ResponseEntity<ApiResponse<StockTransferDTO>> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getStockTransferById(id)));
    }

    @PostMapping("/transfers")
    public ResponseEntity<ApiResponse<StockTransferDTO>> createTransfer(@RequestBody StockTransferDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(inventoryService.createStockTransfer(dto)));
    }

    @PutMapping("/transfers/{id}")
    public ResponseEntity<ApiResponse<StockTransferDTO>> updateTransfer(@PathVariable Long id, @RequestBody StockTransferDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.updateStockTransfer(id, dto)));
    }

    @DeleteMapping("/transfers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransfer(@PathVariable Long id) {
        inventoryService.deleteStockTransfer(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/transfers/{id}/submit")
    public ResponseEntity<ApiResponse<StockTransferDTO>> submitTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.submitStockTransfer(id)));
    }

    @PostMapping("/transfers/{id}/approve")
    public ResponseEntity<ApiResponse<StockTransferDTO>> approveTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.approveStockTransfer(id)));
    }

    @PostMapping("/transfers/{id}/ship")
    public ResponseEntity<ApiResponse<StockTransferDTO>> shipTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.shipStockTransfer(id)));
    }

    @PostMapping("/transfers/{id}/receive")
    public ResponseEntity<ApiResponse<StockTransferDTO>> receiveTransfer(
            @PathVariable Long id,
            @RequestBody(required = false) TransferCompleteRequest request) {
        TransferCompleteRequest req = request != null ? request : new TransferCompleteRequest();
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.completeStockTransfer(id, req)));
    }

    @PostMapping("/transfers/{id}/cancel")
    public ResponseEntity<ApiResponse<StockTransferDTO>> cancelTransfer(
            @PathVariable Long id,
            @RequestBody(required = false) TransferCancelRequest request) {
        TransferCancelRequest req = request != null ? request : new TransferCancelRequest();
        if (req.getCancelReason() == null) {
            req.setCancelReason("Hủy phiếu chuyển kho");
        }
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.cancelStockTransfer(id, req)));
    }

    // ==========================================
    // --- XUẤT HỦY — CANCEL ISSUE ---
    // ==========================================

    @GetMapping("/cancel-issues")
    public ResponseEntity<ApiResponse<List<CancelIssueDTO>>> getAllCancelIssues() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllCancelIssues()));
    }

    @GetMapping("/cancel-issues/{id}")
    public ResponseEntity<ApiResponse<CancelIssueDTO>> getCancelIssueById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getCancelIssueById(id)));
    }

    @PostMapping("/cancel-issues")
    public ResponseEntity<ApiResponse<CancelIssueDTO>> createCancelIssue(@RequestBody CancelIssueDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(inventoryService.createCancelIssue(dto)));
    }

    @PutMapping("/cancel-issues/{id}")
    public ResponseEntity<ApiResponse<CancelIssueDTO>> updateCancelIssue(@PathVariable Long id, @RequestBody CancelIssueDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.updateCancelIssue(id, dto)));
    }

    @DeleteMapping("/cancel-issues/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCancelIssue(@PathVariable Long id) {
        inventoryService.deleteCancelIssue(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/cancel-issues/{id}/submit")
    public ResponseEntity<ApiResponse<CancelIssueDTO>> submitCancelIssue(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.submitCancelIssue(id)));
    }

    @PostMapping("/cancel-issues/{id}/approve")
    public ResponseEntity<ApiResponse<CancelIssueDTO>> approveCancelIssue(
            @PathVariable Long id,
            @RequestBody(required = false) CancelIssueApprovalRequest request) {
        CancelIssueApprovalRequest req = request != null ? request : new CancelIssueApprovalRequest();
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.approveCancelIssue(id, req)));
    }

    @PostMapping("/cancel-issues/{id}/complete")
    public ResponseEntity<ApiResponse<CancelIssueDTO>> completeCancelIssue(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.executeCancelIssue(id)));
    }

    @PatchMapping("/cancel-issues/{id}/cancel")
    public ResponseEntity<ApiResponse<CancelIssueDTO>> cancelCancelIssue(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.cancelCancelIssue(id)));
    }

    // ==========================================
    // --- TRẢ HÀNG NCC — RETURN TO SUPPLIER ---
    // ==========================================

    @GetMapping("/returns-to-suppliers")
    public ResponseEntity<ApiResponse<List<ReturnToSupplierDTO>>> getAllReturns() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllReturnToSuppliers()));
    }

    @GetMapping("/returns-to-suppliers/{id}")
    public ResponseEntity<ApiResponse<ReturnToSupplierDTO>> getReturnById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getReturnToSupplierById(id)));
    }

    @PostMapping("/returns-to-suppliers")
    public ResponseEntity<ApiResponse<ReturnToSupplierDTO>> createReturn(@RequestBody ReturnToSupplierDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(inventoryService.createReturnToSupplier(dto)));
    }

    @PutMapping("/returns-to-suppliers/{id}")
    public ResponseEntity<ApiResponse<ReturnToSupplierDTO>> updateReturn(@PathVariable Long id, @RequestBody ReturnToSupplierDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.updateReturnToSupplier(id, dto)));
    }

    @DeleteMapping("/returns-to-suppliers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReturn(@PathVariable Long id) {
        inventoryService.deleteReturnToSupplier(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/returns-to-suppliers/{id}/submit")
    public ResponseEntity<ApiResponse<ReturnToSupplierDTO>> submitReturn(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.submitReturnToSupplier(id)));
    }

    @PostMapping("/returns-to-suppliers/{id}/approve")
    public ResponseEntity<ApiResponse<ReturnToSupplierDTO>> approveReturn(
            @PathVariable Long id,
            @RequestBody(required = false) ReturnApprovalRequest request) {
        ReturnApprovalRequest req = request != null ? request : new ReturnApprovalRequest();
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.approveReturnToSupplier(id, req)));
    }

    @PostMapping("/returns-to-suppliers/{id}/complete")
    public ResponseEntity<ApiResponse<ReturnToSupplierDTO>> completeReturn(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.executeReturnToSupplier(id)));
    }

    @PatchMapping("/returns-to-suppliers/{id}/cancel")
    public ResponseEntity<ApiResponse<ReturnToSupplierDTO>> cancelReturn(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.cancelReturnToSupplier(id)));
    }
}
