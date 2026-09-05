package org.example.storemanager.modules.purchase.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.purchase.dto.request.CreatePurchaseInvoiceRequest;
import org.example.storemanager.modules.purchase.dto.request.UpdatePurchaseInvoiceRequest;
import org.example.storemanager.modules.purchase.dto.response.PurchaseInvoiceResponse;
import org.example.storemanager.modules.purchase.service.PurchaseInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase/invoices")
@RequiredArgsConstructor
public class PurchaseInvoiceController {

    private final PurchaseInvoiceService purchaseInvoiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseInvoiceResponse>>> getAllInvoices() {
        return ResponseEntity.ok(ApiResponse.ok(purchaseInvoiceService.getAllInvoices()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(purchaseInvoiceService.getInvoiceById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> createInvoice(@Valid @RequestBody CreatePurchaseInvoiceRequest request) {
        PurchaseInvoiceResponse response = purchaseInvoiceService.createInvoice(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePurchaseInvoiceRequest request) {
        PurchaseInvoiceResponse response = purchaseInvoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật hóa đơn mua hàng thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable Long id) {
        purchaseInvoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa hóa đơn mua hàng thành công", null));
    }
}
