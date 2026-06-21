package org.example.storemanager.dto.request.catalog.color;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateColorRequest {

    @NotBlank(message = "Mã màu không được để trống")
    @Size(max = 50, message = "Mã màu không được quá 50 ký tự")
    private String colorCode;

    @NotBlank(message = "Tên màu không được để trống")
    @Size(max = 150, message = "Tên màu không được quá 150 ký tự")
    private String colorName;

    @Size(max = 20, message = "Mã HEX không được quá 20 ký tự")
    private String hexValue;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String description;

    private Boolean isActive = true;
}
