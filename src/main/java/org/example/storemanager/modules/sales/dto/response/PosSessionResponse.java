package org.example.storemanager.modules.sales.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosSessionResponse {
    private Long id;
    private String sessionCode;
    private String terminalCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal openingCash;
    private BigDecimal expectedClosingCash;
    private BigDecimal actualClosingCash;
    private String status;
    private String shiftName;
    private Long userId;
    private String cashierName;
    private Long branchId;
    private String branchName;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal cashRevenue;
    private BigDecimal nonCashRevenue;
}
