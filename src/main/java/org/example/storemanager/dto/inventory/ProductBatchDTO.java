package org.example.storemanager.dto.inventory;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBatchDTO {
    private Long id;
    private String batchNumber;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private String status;
    private Long productId;
    private String productName;
    private String sku;
    private BigDecimal initialUnits;
    private BigDecimal remainingUnits;
    private BigDecimal unitCost;
    private String supplierName;
    private String location;
    private String qualityStatus;
    private String inspector;
    private String notes;
    private String createdBy;
}
