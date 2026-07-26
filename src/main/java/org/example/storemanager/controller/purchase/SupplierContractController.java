package org.example.storemanager.controller.purchase;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.purchase.CreateSupplierContractRequest;
import org.example.storemanager.dto.request.purchase.UpdateSupplierContractRequest;
import org.example.storemanager.dto.response.purchase.SupplierContractResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.entity.catalog.Product;
import org.example.storemanager.service.purchase.SupplierContractService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase/contracts")
@RequiredArgsConstructor
public class SupplierContractController {

    private final SupplierContractService supplierContractService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:create')")
    public ResponseEntity<ApiResponse<SupplierContractResponse>> createContract(@Valid @RequestBody CreateSupplierContractRequest request) {
        SupplierContractResponse response = supplierContractService.createContract(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:update')")
    public ResponseEntity<ApiResponse<SupplierContractResponse>> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierContractRequest request) {
        SupplierContractResponse response = supplierContractService.updateContract(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật hợp đồng thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:update')")
    public ResponseEntity<ApiResponse<SupplierContractResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        SupplierContractResponse response = supplierContractService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái hợp đồng thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteContract(@PathVariable Long id) {
        supplierContractService.deleteContract(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa hợp đồng thành công", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:view')")
    public ResponseEntity<ApiResponse<SupplierContractResponse>> getContractById(@PathVariable Long id) {
        SupplierContractResponse response = supplierContractService.getContractById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:view')")
    public ResponseEntity<ApiResponse<?>> getContracts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            PageResponse<SupplierContractResponse> response = supplierContractService.getContractsPaginated(
                    search, status, supplierId, page, size, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } else {
            List<SupplierContractResponse> response = supplierContractService.getAllContracts(
                    search, status, supplierId, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:update')")
    public ResponseEntity<ApiResponse<SupplierContractResponse>> submitContract(@PathVariable Long id) {
        SupplierContractResponse response = supplierContractService.submitContract(id);
        return ResponseEntity.ok(ApiResponse.ok("Gửi duyệt hợp đồng thành công", response));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:approve')")
    public ResponseEntity<ApiResponse<SupplierContractResponse>> approveContract(@PathVariable Long id) {
        SupplierContractResponse response = supplierContractService.approveContract(id);
        return ResponseEntity.ok(ApiResponse.ok("Duyệt hợp đồng thành công", response));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:activate')")
    public ResponseEntity<ApiResponse<SupplierContractResponse>> activateContract(@PathVariable Long id) {
        SupplierContractResponse response = supplierContractService.activateContract(id);
        return ResponseEntity.ok(ApiResponse.ok("Kích hoạt hợp đồng thành công", response));
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:terminate')")
    public ResponseEntity<ApiResponse<SupplierContractResponse>> terminateContract(@PathVariable Long id) {
        SupplierContractResponse response = supplierContractService.terminateContract(id);
        return ResponseEntity.ok(ApiResponse.ok("Chấm dứt hợp đồng thành công", response));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:renew')")
    public ResponseEntity<ApiResponse<SupplierContractResponse>> renewContract(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEndDate) {
        SupplierContractResponse response = supplierContractService.renewContract(id, newEndDate);
        return ResponseEntity.ok(ApiResponse.ok("Gia hạn hợp đồng thành công", response));
    }

    @GetMapping("/active")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:view')")
    public ResponseEntity<ApiResponse<List<SupplierContractResponse>>> getActiveContracts() {
        List<SupplierContractResponse> response = supplierContractService.getActiveContracts();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/expiring")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:view')")
    public ResponseEntity<ApiResponse<List<SupplierContractResponse>>> getExpiringContracts(@RequestParam(defaultValue = "30") int days) {
        List<SupplierContractResponse> response = supplierContractService.getExpiringContracts(days);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/products")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:view')")
    public ResponseEntity<ApiResponse<List<Product>>> getContractProducts(@PathVariable Long id) {
        List<Product> response = supplierContractService.getContractProducts(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/products")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:update')")
    public ResponseEntity<ApiResponse<Void>> addContractProduct(
            @PathVariable Long id,
            @RequestParam Long productId) {
        supplierContractService.addContractProduct(id, productId);
        return ResponseEntity.ok(ApiResponse.ok("Thêm sản phẩm vào danh mục hợp đồng thành công", null));
    }

    @DeleteMapping("/{id}/products/{productId}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:contract:update')")
    public ResponseEntity<ApiResponse<Void>> removeContractProduct(
            @PathVariable Long id,
            @PathVariable Long productId) {
        supplierContractService.removeContractProduct(id, productId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa sản phẩm khỏi danh mục hợp đồng thành công", null));
    }
}
