package org.example.storemanager.controller.purchase;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.purchase.CreatePurchaseRequest;
import org.example.storemanager.dto.request.purchase.UpdatePurchaseRequest;
import org.example.storemanager.dto.response.purchase.PurchaseRequestResponse;
import org.example.storemanager.dto.response.purchase.PurchaseOrderResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.service.purchase.PurchaseRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase/requests")
@RequiredArgsConstructor
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:create')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> createRequest(@Valid @RequestBody CreatePurchaseRequest request) {
        PurchaseRequestResponse response = purchaseRequestService.createRequest(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:update')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePurchaseRequest request) {
        PurchaseRequestResponse response = purchaseRequestService.updateRequest(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật yêu cầu mua hàng thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:update')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        PurchaseRequestResponse response = purchaseRequestService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái yêu cầu mua hàng thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(@PathVariable Long id) {
        purchaseRequestService.deleteRequest(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa yêu cầu mua hàng thành công", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:view')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> getRequestById(@PathVariable Long id) {
        PurchaseRequestResponse response = purchaseRequestService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:view')")
    public ResponseEntity<ApiResponse<?>> getRequests(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            PageResponse<PurchaseRequestResponse> response = purchaseRequestService.getRequestsPaginated(
                    search, status, branchId, page, size, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } else {
            List<PurchaseRequestResponse> response = purchaseRequestService.getAllRequests(
                    search, status, branchId, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:update')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> submitRequest(@PathVariable Long id) {
        PurchaseRequestResponse response = purchaseRequestService.submitRequest(id);
        return ResponseEntity.ok(ApiResponse.ok("Gửi duyệt yêu cầu mua hàng thành công", response));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:approve')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> approveRequest(@PathVariable Long id) {
        PurchaseRequestResponse response = purchaseRequestService.approveRequest(id);
        return ResponseEntity.ok(ApiResponse.ok("Duyệt yêu cầu mua hàng thành công", response));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:approve')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> rejectRequest(@PathVariable Long id) {
        PurchaseRequestResponse response = purchaseRequestService.rejectRequest(id);
        return ResponseEntity.ok(ApiResponse.ok("Từ chối yêu cầu mua hàng thành công", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:update')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> cancelRequest(@PathVariable Long id) {
        PurchaseRequestResponse response = purchaseRequestService.cancelRequest(id);
        return ResponseEntity.ok(ApiResponse.ok("Hủy yêu cầu mua hàng thành công", response));
    }

    @PostMapping("/{id}/convert-to-order")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:convert')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> convertToOrder(
            @PathVariable Long id,
            @RequestParam Long supplierId) {
        PurchaseOrderResponse response = purchaseRequestService.convertToOrder(id, supplierId);
        return ResponseEntity.ok(ApiResponse.ok("Chuyển đổi yêu cầu mua hàng thành đơn mua hàng thành công", response));
    }

    @GetMapping("/{id}/orders")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:request:view')")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getOrders(@PathVariable Long id) {
        List<PurchaseOrderResponse> response = purchaseRequestService.getOrders(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
