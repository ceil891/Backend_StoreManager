package org.example.storemanager.dto.request.purchase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreatePurchaseRequest {
    @NotBlank(message = "Mã yêu cầu không được để trống")
    private String requestCode;

    @NotNull(message = "Ngày yêu cầu không được để trống")
    private LocalDateTime requestDate;

    private String reason;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    @NotNull(message = "Chi nhánh không được để trống")
    private Long branchId;

    private String note;

    @NotEmpty(message = "Chi tiết yêu cầu không được để trống")
    private List<PurchaseRequestDetailRequest> details;
}
