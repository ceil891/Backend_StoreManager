package org.example.storemanager.modules.wms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseZoneDTO {
    private Long id;
    private String zoneCode;
    private String zoneName;
    private String conditions;
    private BigDecimal capacity;
    private String status;
    private String description;
    private Long branchId;
    private String branchName;
    // Audit info
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
