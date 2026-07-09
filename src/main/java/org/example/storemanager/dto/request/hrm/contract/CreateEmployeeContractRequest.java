package org.example.storemanager.dto.request.hrm.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEmployeeContractRequest {

    @NotBlank
    @Size(max = 50)
    private String contractNumber;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private String contractType;

    @NotBlank
    private String status;

    @NotNull
    private Long userId;

    @NotNull
    private Long positionId;

    private Boolean isActive = true;

    // Additional fields
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
