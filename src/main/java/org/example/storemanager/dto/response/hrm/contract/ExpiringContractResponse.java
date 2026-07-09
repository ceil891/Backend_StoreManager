package org.example.storemanager.dto.response.hrm.contract;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ExpiringContractResponse {
    private Long id;
    private String contractNumber;
    private LocalDate endDate;
    private Long daysRemaining;
    private Long userId;
    private String userName;
    private Long positionId;
    private String positionName;
    private String status;
    private String contractType;
}

