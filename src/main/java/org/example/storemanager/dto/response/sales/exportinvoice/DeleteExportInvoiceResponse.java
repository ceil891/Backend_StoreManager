package org.example.storemanager.dto.response.sales.exportinvoice;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DeleteExportInvoiceResponse {
    private Long id;
    private String invoiceCode;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}