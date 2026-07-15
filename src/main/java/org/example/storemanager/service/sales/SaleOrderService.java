package org.example.storemanager.service.sales;

import org.example.storemanager.dto.request.sales.saleOrder.CreateSaleOrderRequest;
import org.example.storemanager.dto.request.sales.saleOrder.UpdateSaleOrderRequest;
import org.example.storemanager.dto.response.sales.saleOrder.SaleOrderResponse;
import org.example.storemanager.dto.response.sales.saleOrder.DeleteSaleOrderResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.enums.sales.OrderStatus;
import org.example.storemanager.enums.sales.PaymentStatus;

import java.util.List;

public interface SaleOrderService {
    SaleOrderResponse createOrder(CreateSaleOrderRequest request);
    SaleOrderResponse updateOrder(Long id, UpdateSaleOrderRequest request);
    DeleteSaleOrderResponse softDeleteOrder(Long id);
    SaleOrderResponse updateActiveStatus(Long id, Boolean isActive);
    SaleOrderResponse updateOrderStatus(Long id, OrderStatus status);
    SaleOrderResponse updatePaymentStatus(Long id, PaymentStatus status);
    SaleOrderResponse getOrderById(Long id);
    List<SaleOrderResponse> getActiveOrders();
    PageResponse<SaleOrderResponse> searchOrders(String keyword, OrderStatus status, Long branchId, int page, int size, String sortBy, String sortDir);
}