package org.example.storemanager.dto.response.hrm.contract;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CreateEmployeeContractResponse {
    private Long id;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contractType;
    private String status;
    private Long userId;
    private Long positionId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
    private Double salary;
    private Double allowance;
    private Double socialInsuranceSalary;
    private String contractUrl;
    private LocalDate signingDate;
    private Double workingHours;
}
