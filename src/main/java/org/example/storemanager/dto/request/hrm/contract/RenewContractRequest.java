package org.example.storemanager.dto.request.hrm.contract;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RenewContractRequest {

    @NotNull(message = "Ngày gia hạn không được để trống")
    private LocalDate renewalDate;

    @NotNull(message = "Ngày kết thúc mới không được để trống")
    private LocalDate newEndDate;

    private String notes;
}

