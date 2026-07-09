package org.example.storemanager.dto.request.hrm.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TerminateContractRequest {

    @NotNull(message = "Ngày chấm dứt không được để trống")
    private LocalDate terminationDate;

    @NotBlank(message = "Lý do chấm dứt không được để trống")
    private String terminationReason;
}

