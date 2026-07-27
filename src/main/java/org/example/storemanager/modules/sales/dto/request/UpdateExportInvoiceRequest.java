package org.example.storemanager.modules.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateExportInvoiceRequest {
    @NotNull(message = "Ngày hóa đơn không được để trống")
    private LocalDateTime invoiceDate;

    private Long customerId;

    @NotNull(message = "Chi nhánh không được để trống")
    private Long branchId;

    private Long posSessionId;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    private String note;

    private java.math.BigDecimal discount;
    private java.math.BigDecimal tax;

    @NotEmpty(message = "Chi tiết hóa đơn không được để trống")
    private List<ExportInvoiceDetailRequest> details;
}
