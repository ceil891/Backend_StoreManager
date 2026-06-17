package org.example.storemanager.dto.request.catalog.unit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUnitRequest {

    @NotBlank(message = "Mã đơn vị không được để trống")
    @Size(max = 50, message = "Mã đơn vị không được quá 50 ký tự")
    private String unitCode;

    @NotBlank(message = "Tên đơn vị không được để trống")
    @Size(max = 50, message = "Tên đơn vị không được quá 50 ký tự")
    private String unitName;

    @Size(max = 255, message = "Ghi chú không được quá 255 ký tự")
    private String description;

    private Boolean isActive;

    private String unitType;

    private java.math.BigDecimal conversionFactor;

    private String baseUnitCode;

    private Integer precisionDecimals;
}
