package org.example.storemanager.dto.response.catalog.pricelist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualPriceResponse {
    private Long variantId;
    private Long productId;
    private BigDecimal finalPrice;
    private String source; // PRICE_LIST, VARIANT_OVERRIDE, PRODUCT_BASE
    private Long priceListId;
    private String priceListName;
    private String priceSource; // PRICE_LIST, VARIANT_OVERRIDE, PRODUCT_BASE
    private Integer priceListPriority;
    private LocalDateTime resolvedAt;
}
