package org.example.storemanager.modules.inventory.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferShipmentDTO {
    private Long id;
    private String trackingCode;
    private Long transferId;
    private String transferCode;
    private String carrierName;
    private String carrierType;
    private String status;
    private LocalDateTime shippedAt;
    private String fromBranchName;
    private String toBranchName;
    private String logisticsPartner;
    private LocalDateTime createdAt;
}
