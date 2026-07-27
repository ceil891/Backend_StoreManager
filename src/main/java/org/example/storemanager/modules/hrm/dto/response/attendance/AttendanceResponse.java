package org.example.storemanager.modules.hrm.dto.response.attendance;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AttendanceResponse {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDate workDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String gpsLocation;
    private String status;
    private Boolean isActive;
    private Boolean isDeleted;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
