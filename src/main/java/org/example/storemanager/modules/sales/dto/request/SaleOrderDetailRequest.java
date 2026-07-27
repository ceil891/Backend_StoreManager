package org.example.storemanager.modules.sales.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SaleOrderDetailRequest {
    @NotNull(message = "Variant ID không được để trống")
    private Long productVariantId;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private BigDecimal quantity;

    @NotNull(message = "Đơn giá snapshot không được để trống")
    @Positive(message = "Đơn giá phải lớn hơn 0")
    private BigDecimal unitPriceSnapshot;
}
