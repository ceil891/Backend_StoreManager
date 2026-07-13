package org.example.storemanager.dto.request.system.posSession;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePosSessionRequest {
    private BigDecimal openingCash;
    private Long userId;
    private Long branchId;
}
