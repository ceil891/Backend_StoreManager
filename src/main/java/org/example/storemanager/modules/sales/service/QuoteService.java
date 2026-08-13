package org.example.storemanager.modules.sales.service;

import org.example.storemanager.modules.sales.dto.request.CreateQuoteRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateQuoteRequest;
import org.example.storemanager.modules.sales.dto.response.QuoteResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;

import java.util.List;

public interface QuoteService {
    QuoteResponse createQuote(CreateQuoteRequest request);
    QuoteResponse updateQuote(Long id, UpdateQuoteRequest request);
    QuoteResponse updateStatus(Long id, String status);
    void deleteQuote(Long id);
    QuoteResponse getQuoteById(Long id);
    List<QuoteResponse> getAllQuotes(String search, String status, Long branchId, String sort, boolean includeDeleted);
    PageResponse<QuoteResponse> getQuotesPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted);
    QuoteResponse convertToSaleOrder(Long id);
    byte[] generateQuotePdf(Long id);
}
