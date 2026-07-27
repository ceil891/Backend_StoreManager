package org.example.storemanager.modules.catalog.dto.response.unit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUnitResponse {
    private Long id;
    private String unitCode;
    private String unitName;
    private String description;
    private Boolean isActive;
    private String abbreviation;
    private String unitType;
    private java.math.BigDecimal conversionFactor;
    private String baseUnitCode;
    private Integer precisionDecimals;
    private LocalDateTime createdAt;
    private String createdBy;
}
