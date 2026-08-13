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
public class LoyaltyCalculateRequest {
    private Long customerId;
    private BigDecimal netPaidAmount;
}
