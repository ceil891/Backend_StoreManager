package org.example.storemanager.dto.response.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestResponse {
    private Long id;
    private String requestCode;
    private LocalDateTime requestDate;
    private String reason;
    private String status;
    private Long branchId;
    private String branchName;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<PurchaseRequestDetailResponse> details;
}
