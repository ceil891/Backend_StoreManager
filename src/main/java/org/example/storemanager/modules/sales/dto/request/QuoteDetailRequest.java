package org.example.storemanager.modules.sales.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class QuoteDetailRequest {
    private Long productVariantId;
    private Long productId;

    private String sku;
    private String barcode;
    private String description;
    private String unit;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private BigDecimal quantity;

    @NotNull(message = "Đơn giá không được để trống")
    @Positive(message = "Đơn giá phải lớn hơn 0")
    private BigDecimal unitPrice;

    private String discountType; // PERCENT, AMOUNT
    private BigDecimal discountValue;
    private BigDecimal discount; // Fallback amount if provided
    private BigDecimal taxRate;
}
