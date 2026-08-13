package org.example.storemanager.modules.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransactionResponse {
    private Long id;
    private String refCode;
    private String transactionType;
    private Integer pointsChange;
    private Integer currentPoints;
    private String description;
    private LocalDateTime createdAt;
}
