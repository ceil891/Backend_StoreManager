package org.example.storemanager.modules.sales.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.sales.dto.request.CreateSaleOrderRequest;
import org.example.storemanager.modules.sales.dto.response.SaleOrderResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.sales.service.SaleOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * OnlineOrderController - dedicated endpoint for the online store front‑end.
 * It re‑uses the existing SaleOrderService logic but is exposed under
 * "/api/v1/online/orders" and does NOT require authentication.
 */
@RestController
@RequestMapping("/api/v1/online/orders")
@RequiredArgsConstructor
public class OnlineOrderController {

    private final SaleOrderService saleOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<SaleOrderResponse>> createOnlineOrder(@Valid @RequestBody CreateSaleOrderRequest request) {
        // A checkout request is not proof that funds have been received.
        request.setPaymentStatus("UNPAID");
        request.setPaidAmount(java.math.BigDecimal.ZERO);
        request.setStatus("PENDING");
        request.setOrderOrigin("ONLINE");
        SaleOrderResponse response = saleOrderService.createOrder(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SaleOrderResponse>> cancelOnlineOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        SaleOrderResponse response = saleOrderService.cancelOnlineOrder(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Hủy đơn hàng thành công", response));
    }
}
