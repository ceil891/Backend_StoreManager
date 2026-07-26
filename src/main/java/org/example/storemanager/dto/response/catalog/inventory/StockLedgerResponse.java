package org.example.storemanager.dto.response.catalog.inventory;

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
public class StockLedgerResponse {
    private Long id;
    private String transactionType;
    private Long referenceId;
    private String referenceDocument;
    private String productCode;
    private String productName;
    private BigDecimal quantityChange;
    private BigDecimal runningBalance;
    private String branchName;
    private String notes;
    private String createdBy;
    private LocalDateTime transactionDate;
}
