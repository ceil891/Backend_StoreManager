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
public class ComboDetailRequest {

    @NotNull
    private Long productId;

    @NotNull
    private BigDecimal quantity;

    /** Snapshot giá lẻ — dùng cảnh báo khi giá combo > tổng lẻ. */
    private BigDecimal unitPriceAtCreation;
}
