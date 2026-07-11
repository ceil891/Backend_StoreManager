package org.example.storemanager.dto.request.catalog.productunit;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUnitRequest {

    @NotNull(message = "ID đơn vị tính không được để trống")
    private Long unitId;

    @NotNull(message = "Tỷ lệ quy đổi không được để trống")
    private BigDecimal conversionRate;

    @NotNull(message = "Giá bán không được để trống")
    private BigDecimal price;

    @Size(max = 50, message = "Barcode không được quá 50 ký tự")
    private String barcode;
}
