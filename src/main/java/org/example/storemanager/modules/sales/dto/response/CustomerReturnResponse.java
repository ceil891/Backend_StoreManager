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
public class CustomerReturnResponse {
    private Long id;
    private String returnCode;
    private Long returnRequestId;
    private String returnRequestCode;
    private LocalDateTime returnDate;
    private BigDecimal totalRefund;
    private String reason;
    private String status;
    private Long customerId;
    private String customerName;
    private Long invoiceId;
    private String invoiceCode;
    private Long branchId;
    private String branchName;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<CustomerReturnDetailResponse> details;
}
