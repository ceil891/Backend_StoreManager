package org.example.storemanager.service.sales;

import org.example.storemanager.dto.request.sales.CreateExportInvoiceRequest;
import org.example.storemanager.dto.request.sales.UpdateExportInvoiceRequest;
import org.example.storemanager.dto.response.sales.ExportInvoiceResponse;
import org.example.storemanager.dto.response.common.PageResponse;

import java.util.List;

public interface ExportInvoiceService {
    ExportInvoiceResponse createInvoice(CreateExportInvoiceRequest request);
    ExportInvoiceResponse updateInvoice(Long id, UpdateExportInvoiceRequest request);
    ExportInvoiceResponse updateStatus(Long id, String status);
    void deleteInvoice(Long id);
    ExportInvoiceResponse getInvoiceById(Long id);
    List<ExportInvoiceResponse> getAllInvoices(String search, String status, Long branchId, String sort, boolean includeDeleted);
    PageResponse<ExportInvoiceResponse> getInvoicesPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted);
}
