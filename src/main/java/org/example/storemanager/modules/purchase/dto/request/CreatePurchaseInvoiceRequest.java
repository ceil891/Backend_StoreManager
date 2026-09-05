package org.example.storemanager.modules.purchase.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreatePurchaseInvoiceRequest {
    @NotBlank(message = "Mã hóa đơn không được để trống")
    private String invoiceCode;

    private String poCode;
    private Long poId;

    @NotNull(message = "Nhà cung cấp không được để trống")
    private Long supplierId;

    private Long branchId;

    @NotNull(message = "Ngày hóa đơn không được để trống")
    private LocalDateTime invoiceDate;

    private LocalDateTime dueDate;

    private BigDecimal subTotal;
    private BigDecimal vatAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private String status; // CHO_THANH_TOAN, DA_THANH_TOAN, DA_HUY
    private String paymentTerms;
    private String note;
    private java.util.List<org.example.storemanager.modules.purchase.dto.PurchaseInvoiceItemDTO> items;
}
