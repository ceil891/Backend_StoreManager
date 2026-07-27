package org.example.storemanager.modules.sales.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {
    private Long id;
    private String quoteCode;
    private LocalDateTime quoteDate;
    private LocalDateTime validUntil;
    private BigDecimal totalAmount;
    private String status;
    private Long customerId;
    private String customerName;
    private Long branchId;
    private String branchName;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<QuoteDetailResponse> details;
}
