package org.example.storemanager.dto.response.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleOrderDetailResponse {
    private Long id;
    private Long productVariantId;
    private String variantCode;
    private String skuSnapshot;
    private String barcodeSnapshot;
    private String productNameSnapshot;
    private String variantDescriptionSnapshot;
    private BigDecimal quantity;
    private BigDecimal unitPriceSnapshot;
    private BigDecimal subTotal;
}
