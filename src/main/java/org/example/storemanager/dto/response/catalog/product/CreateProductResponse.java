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
public class CreateProductResponse {
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
    private LocalDateTime createdAt;
    private String createdBy;
}
