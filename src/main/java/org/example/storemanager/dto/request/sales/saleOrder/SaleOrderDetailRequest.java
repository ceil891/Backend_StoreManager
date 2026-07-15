package org.example.storemanager.dto.request.sales.saleOrder;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SaleOrderDetailRequest {

    @NotNull(message = "Sản phẩm không được để trống")
    private Long productVariantId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotNull(message = "Đơn giá không được để trống")
    @PositiveOrZero(message = "Đơn giá phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Thành tiền không được để trống")
    private BigDecimal totalAmount;
}