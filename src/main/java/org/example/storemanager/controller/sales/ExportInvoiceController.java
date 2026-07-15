package org.example.storemanager.controller.sales;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.sales.exportinvoice.CreateExportInvoiceRequest;
import org.example.storemanager.dto.request.sales.exportinvoice.UpdateExportInvoiceRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.sales.exportinvoice.DeleteExportInvoiceResponse;
import org.example.storemanager.dto.response.sales.exportinvoice.ExportInvoiceResponse;
import org.example.storemanager.enums.sales.OrderStatus;
import org.example.storemanager.service.sales.ExportInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales/invoices")
@RequiredArgsConstructor
public class ExportInvoiceController {

    private final ExportInvoiceService exportInvoiceService;

    // ========== XEM CHI TIẾT THEO ID ==========
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:view')")
    public ResponseEntity<ApiResponse<ExportInvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        ExportInvoiceResponse response = exportInvoiceService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH PHÂN TRANG ==========
    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:view')")
    public ResponseEntity<ApiResponse<PageResponse<ExportInvoiceResponse>>> getInvoices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<ExportInvoiceResponse> response = exportInvoiceService.getAllPaginated(keyword, branchId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH THEO TRẠNG THÁI ==========
    @GetMapping("/status/{status}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:view')")
    public ResponseEntity<ApiResponse<List<ExportInvoiceResponse>>> getActiveList(@PathVariable String status) {
        List<ExportInvoiceResponse> response = exportInvoiceService.getActiveList(status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== THÊM MỚI ==========
    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:create')")
    public ResponseEntity<ApiResponse<ExportInvoiceResponse>> createInvoice(@RequestBody CreateExportInvoiceRequest request) {
        ExportInvoiceResponse response = exportInvoiceService.create(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== CẬP NHẬT ==========
    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:update')")
    public ResponseEntity<ApiResponse<ExportInvoiceResponse>> updateInvoice(
            @PathVariable Long id,
            @RequestBody UpdateExportInvoiceRequest request) {
        ExportInvoiceResponse response = exportInvoiceService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== CẬP NHẬT TRẠNG THÁI ==========
    @PatchMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:update')")
    public ResponseEntity<ApiResponse<ExportInvoiceResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) { // Sửa từ String status thành Boolean isActive
        ExportInvoiceResponse response = exportInvoiceService.updateStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== CẬP NHẬT TRẠNG THÁI NGHIỆP VỤ BÁN HÀNG (OrderStatus) ==========
    @PatchMapping("/{id}/order-status")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:update')")
    public ResponseEntity<ApiResponse<ExportInvoiceResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        ExportInvoiceResponse response = exportInvoiceService.updateOrderStatus(id, status);

        ApiResponse<ExportInvoiceResponse> apiResponse = ApiResponse.ok(response);
        apiResponse.setMessage("Cập nhật trạng thái hóa đơn thành công");
        return ResponseEntity.ok(apiResponse);
    }

    // ========== XÓA MỀM ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:invoice:delete')")
    public ResponseEntity<ApiResponse<DeleteExportInvoiceResponse>> deleteInvoice(@PathVariable Long id) {
        DeleteExportInvoiceResponse response = exportInvoiceService.delete(id);
        ApiResponse<DeleteExportInvoiceResponse> apiResponse = ApiResponse.ok(response);
        apiResponse.setMessage("Xóa hóa đơn thành công");
        return ResponseEntity.ok(apiResponse);
    }
}