package org.example.storemanager.modules.sales.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePosSessionRequest {
    private String sessionCode;
    private String terminalCode;
    private Long branchId;
    private Long userId;
    private BigDecimal openingCash;
    private String shiftName;
    private String note;
}
