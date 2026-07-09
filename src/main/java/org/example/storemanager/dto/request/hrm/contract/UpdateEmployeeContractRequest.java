package org.example.storemanager.dto.request.hrm.contract;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateEmployeeContractRequest {

    // Make fields optional to allow partial updates. Service will only change non-null fields.
    private String contractNumber;

    private LocalDate startDate;

    private LocalDate endDate;

    private String contractType;

    private String status;

    private Long userId;

    private Long positionId;

    private Boolean isActive;

    // Additional optional fields
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
