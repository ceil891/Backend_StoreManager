package org.example.storemanager.dto.response.catalog.unit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUnitResponse {
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
    private LocalDateTime updatedAt;
    private String updatedBy;
}
