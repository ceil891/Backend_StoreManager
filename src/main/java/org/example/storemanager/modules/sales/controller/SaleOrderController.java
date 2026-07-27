package org.example.storemanager.modules.sales.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.sales.dto.request.CreateSaleOrderRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateSaleOrderRequest;
import org.example.storemanager.modules.sales.dto.response.SaleOrderResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.sales.service.SaleOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales/orders")
@RequiredArgsConstructor
public class SaleOrderController {

    private final SaleOrderService saleOrderService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:create')")
    public ResponseEntity<ApiResponse<SaleOrderResponse>> createOrder(@Valid @RequestBody CreateSaleOrderRequest request) {
        SaleOrderResponse response = saleOrderService.createOrder(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:update')")
    public ResponseEntity<ApiResponse<SaleOrderResponse>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSaleOrderRequest request) {
        SaleOrderResponse response = saleOrderService.updateOrder(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật đơn hàng thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:update')")
    public ResponseEntity<ApiResponse<SaleOrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        SaleOrderResponse response = saleOrderService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái đơn hàng thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        saleOrderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa đơn hàng thành công", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:view')")
    public ResponseEntity<ApiResponse<SaleOrderResponse>> getOrderById(@PathVariable Long id) {
        SaleOrderResponse response = saleOrderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:order:view')")
    public ResponseEntity<ApiResponse<?>> getOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            PageResponse<SaleOrderResponse> response = saleOrderService.getOrdersPaginated(
                    search, status, branchId, page, size, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } else {
            List<SaleOrderResponse> response = saleOrderService.getAllOrders(
                    search, status, branchId, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
    }
}
