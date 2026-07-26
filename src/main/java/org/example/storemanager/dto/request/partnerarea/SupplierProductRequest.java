package org.example.storemanager.dto.request.partnerarea;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProductRequest {

    @NotNull(message = "Nhà cung cấp không được trống")
    private Long supplierId;

    @NotNull(message = "Sản phẩm không được trống")
    private Long productId;

    @Size(max = 100)
    private String supplierSku; // Mã SKU của NCC

    @DecimalMin(value = "0", message = "Giá phải >= 0")
    private BigDecimal unitPrice;

    @Size(max = 10)
    private String currency = "VND";

    @DecimalMin(value = "0", message = "MOQ phải >= 0")
    private BigDecimal moq;

    private Integer leadTimeDays;
    private Boolean isPreferred = false;
    private Boolean isActive = true;
}
