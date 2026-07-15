package org.example.storemanager.service.sales;

import org.example.storemanager.dto.request.sales.exportinvoice.CreateExportInvoiceRequest;
import org.example.storemanager.dto.request.sales.exportinvoice.UpdateExportInvoiceRequest;
import org.example.storemanager.dto.response.sales.exportinvoice.DeleteExportInvoiceResponse;
import org.example.storemanager.dto.response.sales.exportinvoice.ExportInvoiceResponse;
import org.example.storemanager.dto.response.common.PageResponse;

import java.util.List;

public interface ExportInvoiceService {
    ExportInvoiceResponse create(CreateExportInvoiceRequest request);
    ExportInvoiceResponse update(Long id, UpdateExportInvoiceRequest request);
    DeleteExportInvoiceResponse delete(Long id);
    ExportInvoiceResponse updateStatus(Long id, Boolean isActive);
    PageResponse<ExportInvoiceResponse> getAllPaginated(String keyword, Long branchId, int page, int size, String sortBy, String sortDir);
    List<ExportInvoiceResponse> getActiveList(String status);
    ExportInvoiceResponse getById(Long id);
    ExportInvoiceResponse updateOrderStatus(Long id, org.example.storemanager.enums.sales.OrderStatus status);

}