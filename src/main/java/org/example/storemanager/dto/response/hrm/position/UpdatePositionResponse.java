package org.example.storemanager.dto.response.hrm.position;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UpdatePositionResponse {
    private Long id;
    private String positionCode;
    private String positionName;
    private BigDecimal baseSalary;
    private Long departmentId;
    private String description;
    private String positionRank;
    private String managementStatus;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
