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
public class PriceListDetailResponse {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Long productUnitId;
    private String unitCode;
    private String unitName;
    private Boolean isBaseUnit;
    private BigDecimal price;
}
