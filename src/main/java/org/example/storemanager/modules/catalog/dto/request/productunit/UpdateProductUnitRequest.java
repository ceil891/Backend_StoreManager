package org.example.storemanager.modules.catalog.dto.request.productunit;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductUnitRequest {

    @Positive(message = "Tỷ lệ quy đổi phải lớn hơn 0")
    private BigDecimal conversionRate;

    @Positive(message = "Giá bán phải lớn hơn 0")
    private BigDecimal price;

    @Size(max = 50, message = "Barcode không được quá 50 ký tự")
    private String barcode;

    private Boolean isActive;
}
