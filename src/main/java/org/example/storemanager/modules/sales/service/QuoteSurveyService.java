package org.example.storemanager.modules.sales.service;

import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.sales.dto.request.CreateQuoteSurveyRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateQuoteSurveyRequest;
import org.example.storemanager.modules.sales.dto.response.QuoteResponse;
import org.example.storemanager.modules.sales.dto.response.QuoteSurveyResponse;

import java.util.List;

public interface QuoteSurveyService {
    QuoteSurveyResponse createSurvey(CreateQuoteSurveyRequest request);
    QuoteSurveyResponse updateSurvey(Long id, UpdateQuoteSurveyRequest request);
    QuoteSurveyResponse updateStatus(Long id, String status);
    void deleteSurvey(Long id);
    QuoteSurveyResponse getSurveyById(Long id);
    List<QuoteSurveyResponse> getAllSurveys(String search, String status, Long branchId, String sort, boolean includeDeleted);
    PageResponse<QuoteSurveyResponse> getSurveysPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted);
    QuoteResponse convertToQuote(Long surveyId);
}
