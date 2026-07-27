package org.example.storemanager.modules.purchase.service;

import org.example.storemanager.modules.purchase.dto.request.CreatePurchaseRequest;
import org.example.storemanager.modules.purchase.dto.request.UpdatePurchaseRequest;
import org.example.storemanager.modules.purchase.dto.response.PurchaseRequestResponse;
import org.example.storemanager.modules.purchase.dto.response.PurchaseOrderResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;

import java.util.List;

public interface PurchaseRequestService {
    PurchaseRequestResponse createRequest(CreatePurchaseRequest request);
    PurchaseRequestResponse updateRequest(Long id, UpdatePurchaseRequest request);
    PurchaseRequestResponse updateStatus(Long id, String status);
    void deleteRequest(Long id);
    PurchaseRequestResponse getRequestById(Long id);
    List<PurchaseRequestResponse> getAllRequests(String search, String status, Long branchId, String sort, boolean includeDeleted);
    PageResponse<PurchaseRequestResponse> getRequestsPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted);

    PurchaseRequestResponse submitRequest(Long id);
    PurchaseRequestResponse approveRequest(Long id);
    PurchaseRequestResponse rejectRequest(Long id);
    PurchaseRequestResponse cancelRequest(Long id);

    PurchaseOrderResponse convertToOrder(Long id, Long supplierId);
    List<PurchaseOrderResponse> getOrders(Long id);
}
