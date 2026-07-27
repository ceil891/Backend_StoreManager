package org.example.storemanager.modules.purchase.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateSupplierContractRequest {
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    private BigDecimal maxDebtLimit;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    @NotNull(message = "Nhà cung cấp không được để trống")
    private Long supplierId;

    private String contractName;
    private String contractType;
    private LocalDate signedDate;
    private String signedBy;
    private String paymentTerm;
    private String deliveryTerm;
    private String attachment;
    private String note;
}
