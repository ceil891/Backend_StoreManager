package org.example.storemanager.modules.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateCustomerReturnRequest {
    @NotBlank(message = "Mã phiếu trả không được để trống")
    private String returnCode;

    private Long returnRequestId;
    private String returnRequestCode;

    @NotNull(message = "Ngày trả hàng không được để trống")
    private LocalDateTime returnDate;

    private String reason;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    private Long customerId;

    private Long invoiceId;

    private Long orderId;

    @NotNull(message = "Chi nhánh không được để trống")
    private Long branchId;

    private String note;

    @NotEmpty(message = "Chi tiết trả hàng không được để trống")
    private List<CustomerReturnDetailRequest> details;
}
