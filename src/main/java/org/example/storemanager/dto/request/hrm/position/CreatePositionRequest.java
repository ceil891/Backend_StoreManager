package org.example.storemanager.dto.request.hrm.position;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePositionRequest {

    @NotBlank
    @Size(max = 50)
    private String positionCode;

    @NotBlank
    @Size(max = 150)
    private String positionName;

    private BigDecimal baseSalary;

    @NotNull
    private Long departmentId;

    private String positionRank;

    private String managementStatus;

    private String description;

    private Boolean isActive = true;
}
