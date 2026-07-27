package org.example.storemanager.modules.partnerarea.dto.response.customer;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SalesHistoryResponse {
    private Long id;
    private String invoiceCode;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
}