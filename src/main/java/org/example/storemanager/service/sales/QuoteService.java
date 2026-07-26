package org.example.storemanager.service.sales;

import org.example.storemanager.dto.request.sales.CreateQuoteRequest;
import org.example.storemanager.dto.request.sales.UpdateQuoteRequest;
import org.example.storemanager.dto.response.sales.QuoteResponse;
import org.example.storemanager.dto.response.common.PageResponse;

import java.util.List;

public interface QuoteService {
    QuoteResponse createQuote(CreateQuoteRequest request);
    QuoteResponse updateQuote(Long id, UpdateQuoteRequest request);
    QuoteResponse updateStatus(Long id, String status);
    void deleteQuote(Long id);
    QuoteResponse getQuoteById(Long id);
    List<QuoteResponse> getAllQuotes(String search, String status, Long branchId, String sort, boolean includeDeleted);
    PageResponse<QuoteResponse> getQuotesPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted);
}
