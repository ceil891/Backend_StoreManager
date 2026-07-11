package org.example.storemanager.dto.response.catalog.variant;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CreateVariantResponse {
    private Long id;
    private String variantCode;
    private String sku;
    private String barcode;
    private BigDecimal price;
    private String status;
    private Long productId;
    private String productCode;
    private String variantDescription;
    private LocalDateTime createdAt;
    private String createdBy;
}
