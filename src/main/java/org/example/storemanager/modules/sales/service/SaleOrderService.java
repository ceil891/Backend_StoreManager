package org.example.storemanager.modules.sales.service;

import org.example.storemanager.modules.sales.dto.request.CreateSaleOrderRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateSaleOrderRequest;
import org.example.storemanager.modules.sales.dto.response.SaleOrderResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;

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
