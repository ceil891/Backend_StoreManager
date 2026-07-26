package org.example.storemanager.dto.wms;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseBinDTO {
    private Long id;
    private String binCode;
    private String barcode;
    private BigDecimal maxWeightKg;
    private BigDecimal maxVolumeM3;
    private Integer maxPallet;
    private String status;
    private String description;
    // Rack info
    private Long rackId;
    private String rackCode;
    private String rackName;
    // Area info (denormalized for FE convenience)
    private Long areaId;
    private String areaCode;
    // Zone info
    private Long zoneId;
    private String zoneCode;
    // Branch info
    private Long branchId;
    private String branchName;
    // Audit info
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
