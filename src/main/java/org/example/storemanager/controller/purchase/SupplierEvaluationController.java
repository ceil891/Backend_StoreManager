package org.example.storemanager.controller.purchase;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.purchase.CreateSupplierEvaluationRequest;
import org.example.storemanager.dto.request.purchase.UpdateSupplierEvaluationRequest;
import org.example.storemanager.dto.response.purchase.SupplierEvaluationResponse;
import org.example.storemanager.dto.response.purchase.SupplierScoreResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.service.purchase.SupplierEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SupplierEvaluationController {

    private final SupplierEvaluationService supplierEvaluationService;

    @PostMapping("/api/v1/purchase/evaluations")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:evaluation:create')")
    public ResponseEntity<ApiResponse<SupplierEvaluationResponse>> createEvaluation(@Valid @RequestBody CreateSupplierEvaluationRequest request) {
        SupplierEvaluationResponse response = supplierEvaluationService.createEvaluation(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/api/v1/purchase/evaluations/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:evaluation:update')")
    public ResponseEntity<ApiResponse<SupplierEvaluationResponse>> updateEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierEvaluationRequest request) {
        SupplierEvaluationResponse response = supplierEvaluationService.updateEvaluation(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật đánh giá thành công", response));
    }

    @PutMapping("/api/v1/purchase/evaluations/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:evaluation:update')")
    public ResponseEntity<ApiResponse<SupplierEvaluationResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        SupplierEvaluationResponse response = supplierEvaluationService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái đánh giá thành công", response));
    }

    @DeleteMapping("/api/v1/purchase/evaluations/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:evaluation:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteEvaluation(@PathVariable Long id) {
        supplierEvaluationService.deleteEvaluation(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa đánh giá thành công", null));
    }

    @GetMapping("/api/v1/purchase/evaluations/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:evaluation:view')")
    public ResponseEntity<ApiResponse<SupplierEvaluationResponse>> getEvaluationById(@PathVariable Long id) {
        SupplierEvaluationResponse response = supplierEvaluationService.getEvaluationById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/api/v1/purchase/evaluations")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:evaluation:view')")
    public ResponseEntity<ApiResponse<?>> getEvaluations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            PageResponse<SupplierEvaluationResponse> response = supplierEvaluationService.getEvaluationsPaginated(
                    search, supplierId, page, size, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } else {
            List<SupplierEvaluationResponse> response = supplierEvaluationService.getAllEvaluations(
                    search, supplierId, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
    }

    @PostMapping("/api/v1/purchase/evaluations/{id}/submit")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:evaluation:submit')")
    public ResponseEntity<ApiResponse<SupplierEvaluationResponse>> submitEvaluation(@PathVariable Long id) {
        SupplierEvaluationResponse response = supplierEvaluationService.submitEvaluation(id);
        return ResponseEntity.ok(ApiResponse.ok("Gửi duyệt đánh giá thành công", response));
    }

    @PostMapping("/api/v1/purchase/evaluations/{id}/approve")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:evaluation:approve')")
    public ResponseEntity<ApiResponse<SupplierEvaluationResponse>> approveEvaluation(@PathVariable Long id) {
        SupplierEvaluationResponse response = supplierEvaluationService.approveEvaluation(id);
        return ResponseEntity.ok(ApiResponse.ok("Duyệt đánh giá thành công", response));
    }

    @GetMapping("/api/v1/purchase/suppliers/{supplierId}/evaluations")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:evaluation:view')")
    public ResponseEntity<ApiResponse<List<SupplierEvaluationResponse>>> getEvaluationsBySupplier(@PathVariable Long supplierId) {
        List<SupplierEvaluationResponse> response = supplierEvaluationService.getEvaluationsBySupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/api/v1/purchase/suppliers/{supplierId}/score")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:evaluation:view')")
    public ResponseEntity<ApiResponse<SupplierScoreResponse>> getSupplierScore(@PathVariable Long supplierId) {
        SupplierScoreResponse response = supplierEvaluationService.getSupplierScore(supplierId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
