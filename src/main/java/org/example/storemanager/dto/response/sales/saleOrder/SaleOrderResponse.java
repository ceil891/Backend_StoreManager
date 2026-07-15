package org.example.storemanager.dto.response.sales.saleOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storemanager.enums.sales.OrderStatus;
import org.example.storemanager.enums.sales.PaymentStatus;
import org.example.storemanager.enums.sales.OrderOrigin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleOrderResponse {
    private Long id;
    private String orderCode;
    private Long customerId;
    private Long branchId;
    private Long userId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private OrderOrigin orderOrigin;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal finalAmount;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<SaleOrderDetailResponse> details;
}