package org.example.storemanager.modules.purchase.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdatePurchaseOrderRequest {
    @NotNull(message = "Ngày đặt hàng không được để trống")
    private LocalDateTime poDate;

    private LocalDateTime expectedDate;

    @NotNull(message = "Nhà cung cấp không được để trống")
    private Long supplierId;

    @NotNull(message = "Chi nhánh không được để trống")
    private Long branchId;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    private String note;

    private String paymentStatus;
    private java.math.BigDecimal advanceAmount;

    @NotEmpty(message = "Chi tiết đơn hàng không được để trống")
    private List<PurchaseOrderDetailRequest> details;
}
