package org.example.storemanager.dto.response.hrm.contract;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeleteEmployeeContractResponse {
    private Long id;
    private String contractNumber;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
