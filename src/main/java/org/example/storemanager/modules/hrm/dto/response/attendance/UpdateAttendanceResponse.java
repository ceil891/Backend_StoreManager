package org.example.storemanager.modules.hrm.dto.response.attendance;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UpdateAttendanceResponse {
    private Long id;
    private Long userId;
    private LocalDate workDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String gpsLocation;
    private String status;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
