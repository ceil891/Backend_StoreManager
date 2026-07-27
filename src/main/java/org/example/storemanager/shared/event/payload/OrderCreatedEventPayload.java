package org.example.storemanager.shared.event.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEventPayload {
    private String orderId;
    private Long customerId;
    private Long branchId;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
