package org.example.storemanager.modules.sales.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ClosePosSessionRequest {
    private BigDecimal actualClosingCash;
    private String note;
}
