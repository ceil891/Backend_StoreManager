package org.example.storemanager.modules.catalog.dto.response.department;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentResponse {
    private Long id;
    private String deptCode;
    private String deptName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
}
