package org.example.storemanager.dto.response.sales.saleOrder;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SaleOrderDetailResponse {
    private Long id;
    private Long productVariantId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
}