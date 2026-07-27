package org.example.storemanager.shared.event;

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
public class OrderCreatedEvent {
    private String orderId;
    private Long customerId;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private LocalDateTime createdAt;
}
