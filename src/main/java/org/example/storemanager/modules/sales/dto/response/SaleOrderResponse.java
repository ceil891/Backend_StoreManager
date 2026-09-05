package org.example.storemanager.modules.sales.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private LocalDateTime orderDate;
    private LocalDateTime expectedDelivery;
    private BigDecimal subTotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private String status;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private String orderOrigin;
    private String paymentStatus;
    private Long branchId;
    private String branchName;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;
    private Long carrierId;
    private String carrier;
    private Long driverId;
    private String trackingCode;
    private String trackingUrl;
    private String shipperName;
    private String shipperPhone;
    private String deliveryStatus;
    private LocalDateTime assignedAt;
    private String assignedBy;
    private Long paymentMethodId;
    private String paymentMethodCode;
    private List<SaleOrderDetailResponse> details;
}
