package org.example.storemanager.dto.response.hrm.department;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UpdateDepartmentHrmResponse {
    private Long id;
    private String deptCode;
    private String deptName;
    private String description;
    private Long managerId;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
