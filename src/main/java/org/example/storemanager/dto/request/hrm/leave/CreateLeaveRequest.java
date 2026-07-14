package org.example.storemanager.dto.request.hrm.leave;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateLeaveRequest {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotBlank
    private String leaveType;

    private String reason;

    private String attachmentPath;

    @NotNull
    private Long userId;

    private Boolean isActive = true;
}
