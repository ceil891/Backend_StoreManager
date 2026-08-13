package org.example.storemanager.modules.sales.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteDetailResponse {
    private Long id;
    private Long productVariantId;
    private Long productId;
    private String productCode;
    private String productName;
    private String sku;
    private String barcode;
    private String description;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discount;
    private BigDecimal discountAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal subTotal;
    private BigDecimal totalAmount;
}
