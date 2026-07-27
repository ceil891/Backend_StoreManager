package org.example.storemanager.modules.catalog.dto.response.variant;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VariantResponse {

    private Long id;
    private String variantCode;
    private String sku;
    private String barcode;
    private String imageUrl;
    private BigDecimal price;
    private String status;

    private Long productId;
    private String productCode;
    private String productName;

    /**
     * Mô tả tổ hợp thuộc tính, ví dụ: "Size: M | Màu: Đen"
     */
    private String variantDescription;

    private List<VariantAttributeDetail> attributes;

    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    @Data
    @Builder
    public static class VariantAttributeDetail {
        private Long attributeId;
        private String attributeCode;
        private String attributeName;
        private Long valueId;
        private String value;
    }
}
