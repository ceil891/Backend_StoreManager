package org.example.storemanager.dto.inventory;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnToSupplierDTO {
    private Long id;
    private String returnCode;
    private LocalDateTime returnDate;
    private BigDecimal totalAmount;
    private String status;
    private String reason;
    private Long branchId;
    private String branchName;
    private Long supplierId;
    private String supplierName;
    private String grnRefNumber;
    private String createdBy;
    private String note;
    private List<ReturnToSupplierDetailDTO> returnLines;
}
