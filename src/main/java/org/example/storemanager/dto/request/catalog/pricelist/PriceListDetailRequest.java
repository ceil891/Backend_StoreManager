package org.example.storemanager.dto.request.catalog.pricelist;

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
public class PriceListDetailRequest {

    @NotNull
    private Long productId;

    /** Nếu null, backend tự resolve sang ProductUnit gốc (isBaseUnit = true). */
    private Long productUnitId;

    @NotNull
    private BigDecimal price;
}
