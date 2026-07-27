package org.example.storemanager.modules.catalog.dto.response.pricelist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivePriceResponse {
    private Long priceListId;
    private String listCode;
    private String listName;
    private Long productId;
    private Long productUnitId;
    private BigDecimal price;
}
