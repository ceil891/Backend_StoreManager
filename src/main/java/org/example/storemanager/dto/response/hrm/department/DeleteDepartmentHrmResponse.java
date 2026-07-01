package org.example.storemanager.dto.response.hrm.department;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeleteDepartmentHrmResponse {
    private Long id;
    private String deptCode;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
