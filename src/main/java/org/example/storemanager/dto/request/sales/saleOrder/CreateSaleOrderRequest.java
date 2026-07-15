package org.example.storemanager.dto.request.sales.saleOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.example.storemanager.enums.sales.OrderOrigin;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateSaleOrderRequest {

    private Long customerId;

    @NotNull(message = "Chi nhánh không được để trống")
    private Long branchId;

    @NotNull(message = "Nguồn đơn hàng không được để trống")
    private OrderOrigin orderOrigin;

    @NotNull(message = "Tiền hàng không được để trống")
    @PositiveOrZero(message = "Tiền hàng phải lớn hơn hoặc bằng 0")
    private BigDecimal totalAmount;

    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull(message = "Tổng tiền thanh toán không được để trống")
    @PositiveOrZero(message = "Tổng tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal finalAmount;

    @NotEmpty(message = "Chi tiết đơn hàng không được để trống")
    @Valid
    private List<SaleOrderDetailRequest> details;
}