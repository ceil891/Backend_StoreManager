package org.example.storemanager.modules.catalog.dto.response.productunit;

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
public class ProductUnitResponse {
    private Long id;
    private Long productId;
    private Long unitId;
    private String unitCode;
    private String unitName;
    private BigDecimal conversionRate;
    private BigDecimal price;
    private String barcode;
    private Boolean isActive;
    private Boolean isBaseUnit;
    private LocalDateTime createdAt;
    private String createdBy;
    private Boolean isDeleted;
}
