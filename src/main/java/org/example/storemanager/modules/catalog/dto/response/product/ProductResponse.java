package org.example.storemanager.modules.catalog.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storemanager.modules.catalog.dto.response.productunit.ProductUnitResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String productCode;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal costPrice;
    private String brand;
    private String mainImageUrl;
    private String barcode;
    private Boolean isActive;
    private BigDecimal weight;
    private BigDecimal reorderPoint;
    private BigDecimal minStock;
    private BigDecimal maxStock;
    private String galleryImages;
    private String variants;
    private LocalDateTime createdAt;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime updatedAt;

    private Long categoryId;
    private String categoryCode;
    private String categoryName;

    private Long baseUnitId;
    private String baseUnitCode;
    private String baseUnitName;

    private List<ProductUnitResponse> units;
    private List<org.example.storemanager.modules.catalog.dto.response.variant.VariantResponse> variantList;
    private BigDecimal onHand;
}
