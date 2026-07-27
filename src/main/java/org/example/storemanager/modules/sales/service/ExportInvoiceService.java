package org.example.storemanager.modules.sales.service;

import org.example.storemanager.modules.sales.dto.request.CreateExportInvoiceRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateExportInvoiceRequest;
import org.example.storemanager.modules.sales.dto.response.ExportInvoiceResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;

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
