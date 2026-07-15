package org.example.storemanager.controller.sales;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.sales.saleOrder.CreateSaleOrderRequest;
import org.example.storemanager.dto.request.sales.saleOrder.UpdateSaleOrderRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.sales.saleOrder.DeleteSaleOrderResponse;
import org.example.storemanager.dto.response.sales.saleOrder.SaleOrderResponse;
import org.example.storemanager.enums.sales.OrderStatus;
import org.example.storemanager.enums.sales.PaymentStatus;
import org.example.storemanager.service.sales.SaleOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales/orders")
@RequiredArgsConstructor
public class SaleOrderController {

    private final SaleOrderService saleOrderService;

    // ========== TẠO MỚI ==========
    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:create')")
    public ResponseEntity<ApiResponse<SaleOrderResponse>> createOrder(@Valid @RequestBody CreateSaleOrderRequest request) {
        SaleOrderResponse response = saleOrderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    // ========== CẬP NHẬT / CHỈNH SỬA ==========
    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:update')")
    public ResponseEntity<ApiResponse<SaleOrderResponse>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSaleOrderRequest request) {
        SaleOrderResponse response = saleOrderService.updateOrder(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== XÓA MỀM (TẮT HOẠT ĐỘNG) ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:delete')")
    public ResponseEntity<ApiResponse<DeleteSaleOrderResponse>> softDeleteOrder(@PathVariable Long id) {
        DeleteSaleOrderResponse response = saleOrderService.softDeleteOrder(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== CẬP NHẬT TRẠNG THÁI HOẠT ĐỘNG ==========
    @PatchMapping("/{id}/active")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:update-status')")
    public ResponseEntity<ApiResponse<SaleOrderResponse>> updateActiveStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        SaleOrderResponse response = saleOrderService.updateActiveStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== CẬP NHẬT TIẾN ĐỘ ĐƠN HÀNG ==========
    @PatchMapping("/{id}/order-status")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:update-status')")
    public ResponseEntity<ApiResponse<SaleOrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        SaleOrderResponse response = saleOrderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== CẬP NHẬT TRẠNG THÁI THANH TOÁN ==========
    @PatchMapping("/{id}/payment-status")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:update-status')")
    public ResponseEntity<ApiResponse<SaleOrderResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus status) {
        SaleOrderResponse response = saleOrderService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== XEM CHI TIẾT THEO ID ==========
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:view')")
    public ResponseEntity<ApiResponse<SaleOrderResponse>> getOrderById(@PathVariable Long id) {
        SaleOrderResponse response = saleOrderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH ĐANG HOẠT ĐỘNG ==========
    @GetMapping("/active")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:view')")
    public ResponseEntity<ApiResponse<List<SaleOrderResponse>>> getActiveOrders() {
        List<SaleOrderResponse> response = saleOrderService.getActiveOrders();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH PHÂN TRANG & TÌM KIẾM ==========
    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:view')")
    public ResponseEntity<ApiResponse<?>> searchOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok(ApiResponse.ok(
                saleOrderService.searchOrders(keyword, status, branchId, page, size, sortBy, sortDir)
        ));
    }
}