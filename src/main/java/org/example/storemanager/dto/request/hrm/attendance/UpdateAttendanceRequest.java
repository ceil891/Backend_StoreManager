package org.example.storemanager.dto.request.hrm.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UpdateAttendanceRequest {

    @NotNull
    private Long userId;

    @NotNull
    private LocalDate workDate;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private String gpsLocation;

    @NotNull
    private String status;

    private Boolean isActive;
}
