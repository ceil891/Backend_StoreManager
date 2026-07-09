package org.example.storemanager.dto.response.hrm.contract;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RenewContractResponse {
    private Long id;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate renewalDate;
    private String status;
    private Long userId;
    private Long positionId;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
}

