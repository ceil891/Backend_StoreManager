package org.example.storemanager.dto.request.catalog.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAdjustRequest {
    @NotNull(message = "Adjusted quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private BigDecimal adjustedQuantity;
    
    @NotBlank(message = "Reason is required")
    private String reason;
}
