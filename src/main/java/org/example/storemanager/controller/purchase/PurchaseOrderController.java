package org.example.storemanager.controller.purchase;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.purchase.CreatePurchaseOrderRequest;
import org.example.storemanager.dto.request.purchase.UpdatePurchaseOrderRequest;
import org.example.storemanager.dto.request.purchase.CalculatePurchaseOrderRequest;
import org.example.storemanager.dto.response.purchase.CalculatePurchaseOrderResponse;
import org.example.storemanager.dto.response.purchase.PurchaseOrderResponse;
import org.example.storemanager.dto.inventory.ImportReceiptDTO;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.service.purchase.PurchaseOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:create')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> createOrder(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        PurchaseOrderResponse response = purchaseOrderService.createOrder(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:update')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePurchaseOrderRequest request) {
        PurchaseOrderResponse response = purchaseOrderService.updateOrder(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật đơn mua hàng thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:update')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        PurchaseOrderResponse response = purchaseOrderService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái đơn mua hàng thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        purchaseOrderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa đơn mua hàng thành công", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:view')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getOrderById(@PathVariable Long id) {
        PurchaseOrderResponse response = purchaseOrderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:view')")
    public ResponseEntity<ApiResponse<?>> getOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            PageResponse<PurchaseOrderResponse> response = purchaseOrderService.getOrdersPaginated(
                    search, status, branchId, page, size, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } else {
            List<PurchaseOrderResponse> response = purchaseOrderService.getAllOrders(
                    search, status, branchId, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:update')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> submitOrder(@PathVariable Long id) {
        PurchaseOrderResponse response = purchaseOrderService.submitOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Gửi duyệt đơn mua hàng thành công", response));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:approve')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> approveOrder(@PathVariable Long id) {
        PurchaseOrderResponse response = purchaseOrderService.approveOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Duyệt đơn mua hàng thành công", response));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:approve')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> rejectOrder(@PathVariable Long id) {
        PurchaseOrderResponse response = purchaseOrderService.rejectOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Từ chối đơn mua hàng thành công", response));
    }

    @PostMapping("/{id}/send-to-supplier")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:update')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> sendToSupplier(@PathVariable Long id) {
        PurchaseOrderResponse response = purchaseOrderService.sendToSupplier(id);
        return ResponseEntity.ok(ApiResponse.ok("Gửi đơn mua hàng cho nhà cung cấp thành công", response));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:confirm')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> confirmOrder(@PathVariable Long id) {
        PurchaseOrderResponse response = purchaseOrderService.confirmOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Xác nhận nhà cung cấp đồng ý đơn mua hàng thành công", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:cancel')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> cancelOrder(@PathVariable Long id) {
        PurchaseOrderResponse response = purchaseOrderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Hủy đơn mua hàng thành công", response));
    }

    @GetMapping("/{id}/receipts")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:view')")
    public ResponseEntity<ApiResponse<List<ImportReceiptDTO>>> getReceipts(@PathVariable Long id) {
        List<ImportReceiptDTO> response = purchaseOrderService.getReceipts(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/create-receipt")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:receive')")
    public ResponseEntity<ApiResponse<ImportReceiptDTO>> createReceipt(@PathVariable Long id) {
        ImportReceiptDTO response = purchaseOrderService.createReceipt(id);
        return ResponseEntity.ok(ApiResponse.ok("Tạo phiếu nhập kho thành công", response));
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:create')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> duplicateOrder(@PathVariable Long id) {
        PurchaseOrderResponse response = purchaseOrderService.duplicateOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Sao chép đơn mua hàng thành công", response));
    }

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<CalculatePurchaseOrderResponse>> calculateOrder(@Valid @RequestBody CalculatePurchaseOrderRequest request) {
        CalculatePurchaseOrderResponse response = purchaseOrderService.calculateOrder(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
