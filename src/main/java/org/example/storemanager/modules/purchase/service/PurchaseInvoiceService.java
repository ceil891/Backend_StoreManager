package org.example.storemanager.modules.purchase.service;

import org.example.storemanager.modules.purchase.dto.request.CreatePurchaseInvoiceRequest;
import org.example.storemanager.modules.purchase.dto.request.UpdatePurchaseInvoiceRequest;
import org.example.storemanager.modules.purchase.dto.response.PurchaseInvoiceResponse;

import java.util.List;

public interface PurchaseInvoiceService {
    List<PurchaseInvoiceResponse> getAllInvoices();
    PurchaseInvoiceResponse getInvoiceById(Long id);
    PurchaseInvoiceResponse createInvoice(CreatePurchaseInvoiceRequest request);
    PurchaseInvoiceResponse updateInvoice(Long id, UpdatePurchaseInvoiceRequest request);
    void deleteInvoice(Long id);
}
