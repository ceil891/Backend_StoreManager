package org.example.storemanager.modules.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateQuoteRequest {
    @NotBlank(message = "Mã báo giá không được để trống")
    private String quoteCode;

    @NotNull(message = "Ngày báo giá không được để trống")
    private LocalDateTime quoteDate;

    private LocalDateTime validUntil;

    private String currency; // VND, USD
    private String paymentTerms;
    private String deliveryTerms;
    private String warrantyTerms;
    private String validityTerms;
    private String shippingAddress;

    private BigDecimal subTotal;
    private String discountType; // PERCENT, AMOUNT
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;

    @NotNull(message = "Khách hàng không được để trống")
    private Long customerId;

    @NotNull(message = "Chi nhánh không được để trống")
    private Long branchId;

    private Long warehouseId;
    private String warehouseName;

    private Long salesPersonId;
    private String salesPersonName;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    private String note;
    private String attachments;

    @NotEmpty(message = "Chi tiết báo giá không được để trống")
    private List<QuoteDetailRequest> details;
}
