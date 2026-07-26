package org.example.storemanager.dto.inventory;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelIssueDTO {
    private Long id;
    private String cancelCode;
    private LocalDateTime cancelDate;
    private BigDecimal totalValue;
    private String reason;
    private String status;
    private Long branchId;
    private String branchName;
    private String createdBy;
    private String note;
    private List<CancelIssueDetailDTO> cancelLines;
}
