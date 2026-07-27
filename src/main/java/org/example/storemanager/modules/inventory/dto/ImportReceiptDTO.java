package org.example.storemanager.modules.inventory.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportReceiptDTO {
    private Long id;
    private String receiptCode;
    private LocalDateTime receiptDate;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal tax;
    private String status;
    private Long branchId;
    private String branchName;
    private Long supplierId;
    private String supplierName;
    private Long purchaseOrderId;
    private String purchaseOrderCode;
    private String createdBy;
    private String inspectedBy;
    private String note;
    private List<ImportReceiptDetailDTO> receiptLines;
}
