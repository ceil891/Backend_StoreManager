package org.example.storemanager.dto.request.hrm.position;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePositionRequest {

    @Size(max = 150)
    private String positionName;

    private BigDecimal baseSalary;

    private Long departmentId;

    private String positionRank;

    @JsonAlias({"managementStatus", "management_status_id"})
    private Long managementStatusId;

    private String description;

    private Boolean isActive;
}
