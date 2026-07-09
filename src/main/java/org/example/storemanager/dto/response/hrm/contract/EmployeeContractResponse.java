package org.example.storemanager.dto.response.hrm.contract;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeContractResponse {
    private Long id;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contractType;
    private String status;
    private Long userId;
    private String userName;
    private Long positionId;
    private String positionName;
    private Double salary;
    private Double allowance;
    private LocalDate renewalDate;
    private LocalDate terminationDate;
    private String terminationReason;
    private String contractUrl;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
