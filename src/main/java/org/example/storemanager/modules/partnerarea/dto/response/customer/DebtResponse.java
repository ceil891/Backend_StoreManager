package org.example.storemanager.modules.partnerarea.dto.response.customer;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DebtResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String description;
}
