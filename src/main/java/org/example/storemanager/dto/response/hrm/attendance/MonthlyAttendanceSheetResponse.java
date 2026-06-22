package org.example.storemanager.dto.response.hrm.attendance;

import lombok.Builder;
import lombok.Data;
import org.example.storemanager.dto.response.hrm.attendance.AttendanceResponse;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MonthlyAttendanceSheetResponse {
    private Integer month;
    private Integer year;
    private Long userId;
    private Long departmentId;
    private int workDays;
    private int lateCount;
    private int earlyLeaveCount;
    private BigDecimal overtimeHours;
    private List<AttendanceResponse> details;
}
