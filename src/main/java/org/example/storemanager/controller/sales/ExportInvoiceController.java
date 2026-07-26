package org.example.storemanager.controller.sales;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.sales.CreateExportInvoiceRequest;
import org.example.storemanager.dto.request.sales.UpdateExportInvoiceRequest;
import org.example.storemanager.dto.response.sales.ExportInvoiceResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.service.sales.ExportInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales/invoices")
@RequiredArgsConstructor
public class ExportInvoiceController {

    private final ExportInvoiceService exportInvoiceService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:create')")
    public ResponseEntity<ApiResponse<ExportInvoiceResponse>> createInvoice(@Valid @RequestBody CreateExportInvoiceRequest request) {
        ExportInvoiceResponse response = exportInvoiceService.createInvoice(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:update')")
    public ResponseEntity<ApiResponse<ExportInvoiceResponse>> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExportInvoiceRequest request) {
        ExportInvoiceResponse response = exportInvoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật hóa đơn thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:update')")
    public ResponseEntity<ApiResponse<ExportInvoiceResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ExportInvoiceResponse response = exportInvoiceService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái hóa đơn thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable Long id) {
        exportInvoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa hóa đơn thành công", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:view')")
    public ResponseEntity<ApiResponse<ExportInvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        ExportInvoiceResponse response = exportInvoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:view')")
    public ResponseEntity<ApiResponse<?>> getInvoices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            PageResponse<ExportInvoiceResponse> response = exportInvoiceService.getInvoicesPaginated(
                    search, status, branchId, page, size, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } else {
            List<ExportInvoiceResponse> response = exportInvoiceService.getAllInvoices(
                    search, status, branchId, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
    }
}
