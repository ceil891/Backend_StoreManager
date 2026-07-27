package org.example.storemanager.modules.inventory.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportReceiptDetailDTO {
    private Long id;
    private Long productVariantId;
    private String productName;
    private String sku;
    private String barcode;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal subTotal;
    private String batchNumber;
    private LocalDate expiryDate;
    private Long targetBinId;
    private String targetBinCode;
}

