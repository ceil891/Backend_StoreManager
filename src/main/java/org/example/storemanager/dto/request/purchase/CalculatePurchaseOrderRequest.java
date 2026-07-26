package org.example.storemanager.dto.request.purchase;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculatePurchaseOrderRequest {
    private Long supplierId;
    private List<CalculateItem> items;
    private BigDecimal taxRate; // Ví dụ: 10 đại diện cho 10%
    private BigDecimal shippingFee;

    @Data
    public static class CalculateItem {
        private Long productId;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal discount; // Phần trăm chiết khấu, ví dụ 5 cho 5%
    }
}
