package org.example.storemanager.modules.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateQuoteRequest {
    @NotNull(message = "Ngày báo giá không được để trống")
    private LocalDateTime quoteDate;

    private LocalDateTime validUntil;

    @NotNull(message = "Khách hàng không được để trống")
    private Long customerId;

    @NotNull(message = "Chi nhánh không được để trống")
    private Long branchId;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    private String note;

    @NotEmpty(message = "Chi tiết báo giá không được để trống")
    private List<QuoteDetailRequest> details;
}
