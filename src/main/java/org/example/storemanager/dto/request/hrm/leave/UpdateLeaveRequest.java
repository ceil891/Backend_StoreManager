package org.example.storemanager.dto.request.hrm.leave;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateLeaveRequest {
    private LocalDate startDate;

    private LocalDate endDate;

    private String leaveType;

    private String reason;

    private String attachmentPath;

    private Long userId;

    private Boolean isActive;
}
