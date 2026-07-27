package org.example.storemanager.modules.sales.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CustomerReturnDetailRequest {
    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private BigDecimal quantity;

    @NotNull(message = "Giá hoàn trả không được để trống")
    @Positive(message = "Giá hoàn trả phải lớn hơn 0")
    private BigDecimal refundPrice;
}
