package org.example.storemanager.modules.partnerarea.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProductResponse {
    private Long id;
    private String supplierSku;
    private BigDecimal unitPrice;
    private String currency;
    private BigDecimal moq;
    private Integer leadTimeDays;
    private Boolean isPreferred;
    private Boolean isActive;
    // Supplier info
    private Long supplierId;
    private String supplierName;
    private String supplierCode;
    // Product info
    private Long productId;
    private String productName;
    private String productCode;
    private String mainImageUrl;
}
