package org.example.storemanager.dto.response.hrm.department;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DepartmentHrmResponse {
    private Long id;
    private String deptCode;
    private String deptName;
    private String description;
    private Long managerId;
    private String managerName;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
