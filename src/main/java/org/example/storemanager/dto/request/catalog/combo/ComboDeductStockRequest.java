package org.example.storemanager.dto.request.catalog.combo;

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
public class ComboDeductStockRequest {

    @NotNull
    private BigDecimal quantity;

    private Long warehouseZoneId;
    private Long branchId;
    private String referenceDocument;
}
