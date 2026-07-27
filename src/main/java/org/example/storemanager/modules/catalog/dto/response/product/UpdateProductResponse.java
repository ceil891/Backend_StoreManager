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
public class UpdateProductResponse {
    private Long id;
    private String productCode;
    private String name;
    private Long categoryId;
    private Long baseUnitId;
    private BigDecimal basePrice;
    private BigDecimal costPrice;
    private String mainImageUrl;
    private String galleryImages;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
