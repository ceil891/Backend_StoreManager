package org.example.storemanager.dto.response.purchase;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CalculatePurchaseOrderResponse {
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
}
