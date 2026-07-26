package org.example.storemanager.service.sales;

import org.example.storemanager.dto.request.sales.CreateSaleOrderRequest;
import org.example.storemanager.dto.request.sales.UpdateSaleOrderRequest;
import org.example.storemanager.dto.response.sales.SaleOrderResponse;
import org.example.storemanager.dto.response.common.PageResponse;

import java.util.List;

public interface SaleOrderService {
    SaleOrderResponse createOrder(CreateSaleOrderRequest request);
    SaleOrderResponse updateOrder(Long id, UpdateSaleOrderRequest request);
    SaleOrderResponse updateStatus(Long id, String status);
    void deleteOrder(Long id);
    SaleOrderResponse getOrderById(Long id);
    List<SaleOrderResponse> getAllOrders(String search, String status, Long branchId, String sort, boolean includeDeleted);
    PageResponse<SaleOrderResponse> getOrdersPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted);
}
