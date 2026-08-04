package org.example.storemanager.modules.cart.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequest {

    @NotNull(message = "quantity không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    @Max(value = 999, message = "Số lượng tối đa là 999")
    private Integer quantity; // 0 = xóa item
}
