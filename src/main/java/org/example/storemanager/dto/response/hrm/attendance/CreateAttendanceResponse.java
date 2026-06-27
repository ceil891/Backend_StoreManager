package org.example.storemanager.dto.response.hrm.attendance;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CreateAttendanceResponse {
    private Long id;
    private Long userId;
    private LocalDate workDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String gpsLocation;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
}
