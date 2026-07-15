package org.example.storemanager.dto.request.sales.exportinvoice;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateExportInvoiceRequest {
    private LocalDateTime invoiceDate;
    private BigDecimal discount;
    private BigDecimal tax;
    private String status;
    // Thường hóa đơn đã tạo sẽ hạn chế sửa chi tiết,
    // chỉ sửa trạng thái hoặc các thông tin phụ.
}