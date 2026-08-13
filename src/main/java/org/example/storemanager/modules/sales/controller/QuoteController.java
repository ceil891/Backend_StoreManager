package org.example.storemanager.modules.sales.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.sales.dto.request.CreateQuoteRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateQuoteRequest;
import org.example.storemanager.modules.sales.dto.response.QuoteResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.sales.service.QuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:quote:create')")
    public ResponseEntity<ApiResponse<QuoteResponse>> createQuote(@Valid @RequestBody CreateQuoteRequest request) {
        QuoteResponse response = quoteService.createQuote(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:quote:update')")
    public ResponseEntity<ApiResponse<QuoteResponse>> updateQuote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuoteRequest request) {
        QuoteResponse response = quoteService.updateQuote(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật báo giá thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:quote:update')")
    public ResponseEntity<ApiResponse<QuoteResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        QuoteResponse response = quoteService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái báo giá thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:quote:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteQuote(@PathVariable Long id) {
        quoteService.deleteQuote(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa báo giá thành công", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:quote:view')")
    public ResponseEntity<ApiResponse<QuoteResponse>> getQuoteById(@PathVariable Long id) {
        QuoteResponse response = quoteService.getQuoteById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:quote:view')")
    public ResponseEntity<ApiResponse<?>> getQuotes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            PageResponse<QuoteResponse> response = quoteService.getQuotesPaginated(
                    search, status, branchId, page, size, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } else {
            List<QuoteResponse> response = quoteService.getAllQuotes(
                    search, status, branchId, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
    }

    @PostMapping("/{id}/convert-to-order")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:quote:update')")
    public ResponseEntity<ApiResponse<QuoteResponse>> convertToSaleOrder(@PathVariable Long id) {
        QuoteResponse response = quoteService.convertToSaleOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Chuyển báo giá thành đơn bán hàng thành công", response));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:quote:view')")
    public ResponseEntity<byte[]> generateQuotePdf(@PathVariable Long id) {
        byte[] pdfBytes = quoteService.generateQuotePdf(id);
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .header("Content-Disposition", "inline; filename=Quote-" + id + ".html")
                .body(pdfBytes);
    }
}
