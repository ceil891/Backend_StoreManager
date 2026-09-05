package org.example.storemanager.modules.catalog.dto.response.product;

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
    private String createdBy;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;

    private Long categoryId;
    private String categoryName;

    private Long baseUnitId;
    private String baseUnitCode;
    private String baseUnitName;

    /** Tổng tồn kho vật lý hiện tại (tổng từ size_inventory) */
    private BigDecimal onHand;

    private org.example.storemanager.shared.enums.catalog.TaxClass taxClass;
    private BigDecimal vatRate;
}
