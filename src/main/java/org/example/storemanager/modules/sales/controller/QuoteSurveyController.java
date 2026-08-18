package org.example.storemanager.modules.sales.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.sales.dto.request.CreateQuoteSurveyRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateQuoteSurveyRequest;
import org.example.storemanager.modules.sales.dto.response.QuoteResponse;
import org.example.storemanager.modules.sales.dto.response.QuoteSurveyResponse;
import org.example.storemanager.modules.sales.service.QuoteSurveyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/sales/quote-surveys", "/api/v1/sales/offers"})
@RequiredArgsConstructor
public class QuoteSurveyController {

    private final QuoteSurveyService surveyService;

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:offer:create') || @securityEvaluator.hasPermission('sales:quote:create')")
    public ResponseEntity<ApiResponse<QuoteSurveyResponse>> createSurvey(@Valid @RequestBody CreateQuoteSurveyRequest request) {
        QuoteSurveyResponse response = surveyService.createSurvey(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:offer:update') || @securityEvaluator.hasPermission('sales:quote:update')")
    public ResponseEntity<ApiResponse<QuoteSurveyResponse>> updateSurvey(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuoteSurveyRequest request) {
        QuoteSurveyResponse response = surveyService.updateSurvey(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật khảo sát báo giá thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:offer:update') || @securityEvaluator.hasPermission('sales:quote:update')")
    public ResponseEntity<ApiResponse<QuoteSurveyResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        QuoteSurveyResponse response = surveyService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái khảo sát thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:offer:delete') || @securityEvaluator.hasPermission('sales:quote:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteSurvey(@PathVariable Long id) {
        surveyService.deleteSurvey(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa khảo sát báo giá thành công", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:offer:view') || @securityEvaluator.hasPermission('sales:quote:view')")
    public ResponseEntity<ApiResponse<QuoteSurveyResponse>> getSurveyById(@PathVariable Long id) {
        QuoteSurveyResponse response = surveyService.getSurveyById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('sales:offer:view') || @securityEvaluator.hasPermission('sales:quote:view')")
    public ResponseEntity<ApiResponse<?>> getSurveys(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        if (page != null && size != null) {
            PageResponse<QuoteSurveyResponse> response = surveyService.getSurveysPaginated(
                    search, status, branchId, page, size, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } else {
            List<QuoteSurveyResponse> response = surveyService.getAllSurveys(
                    search, status, branchId, sort, includeDeleted);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
    }

    @PostMapping("/{id}/convert-to-quote")
    @PreAuthorize("@securityEvaluator.hasPermission('sales:offer:update') || @securityEvaluator.hasPermission('sales:quote:create')")
    public ResponseEntity<ApiResponse<QuoteResponse>> convertToQuote(@PathVariable Long id) {
        QuoteResponse response = surveyService.convertToQuote(id);
        return ResponseEntity.ok(ApiResponse.ok("Chuyển khảo sát thành báo giá bán hàng thành công", response));
    }
}
