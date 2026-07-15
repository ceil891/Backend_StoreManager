package org.example.storemanager.dto.request.sales.saleOrder;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.storemanager.enums.sales.OrderStatus;
import org.example.storemanager.enums.sales.PaymentStatus;

import java.math.BigDecimal;

@Data
public class UpdateSaleOrderRequest {
    private Long customerId;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal finalAmount;
}