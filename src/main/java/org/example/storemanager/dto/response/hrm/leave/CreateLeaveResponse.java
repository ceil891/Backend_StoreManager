package org.example.storemanager.dto.response.hrm.leave;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CreateLeaveResponse {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveType;
    private String reason;
    private String status;
    private Integer numberOfDays;
    private String attachmentPath;
    private Long userId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
}
