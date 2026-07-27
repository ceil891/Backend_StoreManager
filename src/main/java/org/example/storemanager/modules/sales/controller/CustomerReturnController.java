package org.example.storemanager.modules.sales.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.sales.dto.request.CreateCustomerReturnRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateCustomerReturnRequest;
import org.example.storemanager.modules.sales.dto.response.CustomerReturnResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.sales.service.CustomerReturnService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales/returns")
@RequiredArgsConstructor
public class CustomerReturnController {

    private final CustomerReturnService customerReturnService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:return:create')")
    public ResponseEntity<ApiResponse<CustomerReturnResponse>> createReturn(@Valid @RequestBody CreateCustomerReturnRequest request) {
        CustomerReturnResponse response = customerReturnService.createReturn(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:return:update')")
    public ResponseEntity<ApiResponse<CustomerReturnResponse>> updateReturn(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerReturnRequest request) {
        CustomerReturnResponse response = customerReturnService.updateReturn(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật phiếu trả hàng thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:return:update')")
    public ResponseEntity<ApiResponse<CustomerReturnResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        CustomerReturnResponse response = customerReturnService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái trả hàng thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:return:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteReturn(@PathVariable Long id) {
        customerReturnService.deleteReturn(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa phiếu trả hàng thành công", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:return:view')")
    public ResponseEntity<ApiResponse<CustomerReturnResponse>> getReturnById(@PathVariable Long id) {
        CustomerReturnResponse response = customerReturnService.getReturnById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:return:view')")
    public ResponseEntity<ApiResponse<?>> getReturns(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            PageResponse<CustomerReturnResponse> response = customerReturnService.getReturnsPaginated(
                    search, status, branchId, page, size, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } else {
            List<CustomerReturnResponse> response = customerReturnService.getAllReturns(
                    search, status, branchId, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
    }
}
