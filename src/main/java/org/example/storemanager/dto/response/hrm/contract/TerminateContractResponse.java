package org.example.storemanager.dto.response.hrm.contract;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TerminateContractResponse {
    private Long id;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate terminationDate;
    private String terminationReason;
    private String status;
    private Long userId;
    private Long positionId;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
}

