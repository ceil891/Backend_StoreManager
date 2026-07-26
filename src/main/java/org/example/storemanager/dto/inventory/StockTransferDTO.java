package org.example.storemanager.dto.inventory;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferDTO {
    private Long id;
    private String transferCode;
    private LocalDateTime transferDate;
    private String status;
    private Long fromBranchId;
    private String fromBranchName;
    private Long toBranchId;
    private String toBranchName;
    private String logisticsPartner;
    private String trackingRef;
    private String requestedBy;
    private String approvedBy;
    private LocalDateTime estArrivalDate;
    private String createdBy;
    private String note;
    private List<StockTransferDetailDTO> transferLines;
}
