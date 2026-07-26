package org.example.storemanager.dto.request.sales;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateCustomerReturnRequest {
    @NotNull(message = "Ngày trả hàng không được để trống")
    private LocalDateTime returnDate;

    private String reason;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    private Long customerId;

    @NotNull(message = "Hóa đơn tham chiếu không được để trống")
    private Long invoiceId;

    @NotNull(message = "Chi nhánh không được để trống")
    private Long branchId;

    private String note;

    @NotEmpty(message = "Chi tiết trả hàng không được để trống")
    private List<CustomerReturnDetailRequest> details;
}
