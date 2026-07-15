package org.example.storemanager.dto.response.hrm.leave;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaveBalanceResponse {
    private Long userId;
    private String userName;
    private Integer totalLeaveAllowed;
    private Integer usedLeaveDays;
    private Integer remainingLeaveDays;
    private Integer pendingLeaveDays;
}
