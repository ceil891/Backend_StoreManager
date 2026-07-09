package org.example.storemanager.dto.response.hrm.position;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PositionResponse {
    private Long id;
    private String positionCode;
    private String positionName;
    private BigDecimal baseSalary;
    private Long departmentId;
    private String departmentName;
    private String positionRank;
    private String managementStatus;
    private String description;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
