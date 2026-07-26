package org.example.storemanager.dto.request.sales;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class QuoteDetailRequest {
    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private BigDecimal quantity;

    @NotNull(message = "Đơn giá không được để trống")
    @Positive(message = "Đơn giá phải lớn hơn 0")
    private BigDecimal unitPrice;

    private BigDecimal discount;
}
