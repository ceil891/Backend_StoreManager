package org.example.storemanager.modules.purchase.service;

import org.example.storemanager.modules.purchase.dto.request.CreatePurchaseOrderRequest;
import org.example.storemanager.modules.purchase.dto.request.UpdatePurchaseOrderRequest;
import org.example.storemanager.modules.purchase.dto.request.CalculatePurchaseOrderRequest;
import org.example.storemanager.modules.purchase.dto.response.CalculatePurchaseOrderResponse;
import org.example.storemanager.modules.purchase.dto.response.PurchaseOrderResponse;
import org.example.storemanager.modules.inventory.dto.ImportReceiptDTO;
import org.example.storemanager.modules.common.dto.response.PageResponse;

import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrderResponse createOrder(CreatePurchaseOrderRequest request);
    PurchaseOrderResponse updateOrder(Long id, UpdatePurchaseOrderRequest request);
    PurchaseOrderResponse updateStatus(Long id, String status);
    void deleteOrder(Long id);
    PurchaseOrderResponse getOrderById(Long id);
    List<PurchaseOrderResponse> getAllOrders(String search, String status, Long branchId, String sort, boolean includeDeleted);
    PageResponse<PurchaseOrderResponse> getOrdersPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted);

    PurchaseOrderResponse submitOrder(Long id);
    PurchaseOrderResponse approveOrder(Long id);
    PurchaseOrderResponse rejectOrder(Long id);
    PurchaseOrderResponse sendToSupplier(Long id);
    PurchaseOrderResponse confirmOrder(Long id);
    PurchaseOrderResponse cancelOrder(Long id);

    List<ImportReceiptDTO> getReceipts(Long id);
    ImportReceiptDTO createReceipt(Long id);

    PurchaseOrderResponse duplicateOrder(Long id);
    CalculatePurchaseOrderResponse calculateOrder(CalculatePurchaseOrderRequest request);
}
