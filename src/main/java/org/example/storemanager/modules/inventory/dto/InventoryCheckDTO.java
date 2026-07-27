package org.example.storemanager.modules.inventory.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckDTO {
    private Long id;
    private String checkCode;
    private LocalDateTime checkDate;
    private String status;
    
    @NotNull(message = "Branch ID is required")
    private Long branchId;
    
    private String branchName;
    
    @NotNull(message = "Warehouse Zone ID is required")
    private Long warehouseZoneId;
    
    private String warehouseZoneName;

    private Integer totalItems;
    private Integer discrepancyCount;
    private BigDecimal netVariance;
    private String createdBy;

    @NotEmpty(message = "Check details cannot be empty")
    private List<InventoryCheckDetailDTO> checkLines;
}
