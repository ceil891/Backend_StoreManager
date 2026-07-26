package org.example.storemanager.service.purchase;

import org.example.storemanager.dto.request.purchase.CreatePurchaseOrderRequest;
import org.example.storemanager.dto.request.purchase.UpdatePurchaseOrderRequest;
import org.example.storemanager.dto.request.purchase.CalculatePurchaseOrderRequest;
import org.example.storemanager.dto.response.purchase.CalculatePurchaseOrderResponse;
import org.example.storemanager.dto.response.purchase.PurchaseOrderResponse;
import org.example.storemanager.dto.inventory.ImportReceiptDTO;
import org.example.storemanager.dto.response.common.PageResponse;

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
