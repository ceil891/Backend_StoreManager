package org.example.storemanager.modules.purchase.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierContractResponse {
    private Long id;
    private String contractCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal maxDebtLimit;
    private String status;
    private Long supplierId;
    private String supplierName;
    private String contractName;
    private String contractType;
    private LocalDate signedDate;
    private String signedBy;
    private String paymentTerm;
    private String deliveryTerm;
    private String attachment;
    private LocalDate renewalDate;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;
}
