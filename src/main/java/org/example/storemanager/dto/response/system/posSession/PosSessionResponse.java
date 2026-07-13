package org.example.storemanager.dto.response.system.posSession;

import lombok.*;
import org.example.storemanager.enums.system.PosSessionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosSessionResponse {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal openingCash;
    private BigDecimal actualClosingCash;
    private PosSessionStatus status;
    private String username;
    private String branchName;
}