package org.example.storemanager.dto.response.hrm.contract;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UpdateEmployeeContractResponse {
    private Long id;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contractType;
    private String status;
    private Long userId;
    private Long positionId;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Double salary;
    private Double allowance;
    private Double socialInsuranceSalary;
    private String contractUrl;
    private LocalDate signingDate;
    private Double workingHours;
    private LocalDate renewalDate;
    private LocalDate terminationDate;
    private String terminationReason;
}
