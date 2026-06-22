package org.example.storemanager.dto.request.advancedaccounting;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdjustAttendanceRequest {
    private Long userId;
    private Long attendanceId;
    private LocalDate workDate;
    private LocalDateTime requestedCheckInTime;
    private LocalDateTime requestedCheckOutTime;
    private String reason;
}