package org.example.storemanager.dto.response.hrm.contract;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ContractHistoryResponse {
    private Long id;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contractType;
    private String status;
    private Long userId;
    private String userName;
    private Long positionId;
    private String positionName;
    private Double salary;
    private Double allowance;
    private LocalDate terminationDate;
    private String terminationReason;
    private LocalDate renewalDate;
    private String contractUrl;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
}

