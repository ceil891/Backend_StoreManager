package org.example.storemanager.modules.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyCalculateResponse {
    private Long customerId;
    private String customerName;
    private BigDecimal netPaidAmount;
    private BigDecimal amountPerPoint;
    private String tierCode;
    private String tierName;
    private BigDecimal tierMultiplier;
    private Integer expectedPointsEarned;
    private Integer currentPoints;
    private Integer expectedBalanceAfter;
}
