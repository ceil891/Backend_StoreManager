package org.example.storemanager.dto.response.catalog.product;

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
public class MapProductResponse {
    private Long id;
    private String productCode;
    private String name;
    private BigDecimal basePrice;
    private BigDecimal costPrice;
    private String brand;
    private String mainImageUrl;
    private String barcode;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Boolean isDeleted;

    private Long categoryId;
    private String categoryName;

    private Long baseUnitId;
    private String baseUnitCode;
    private String baseUnitName;
}
