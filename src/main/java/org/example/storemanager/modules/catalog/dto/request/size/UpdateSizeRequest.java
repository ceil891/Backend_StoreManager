package org.example.storemanager.modules.catalog.dto.request.size;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSizeRequest {

    @NotBlank(message = "Mã kích thước không được để trống")
    @Size(max = 50, message = "Mã kích thước không được quá 50 ký tự")
    private String sizeCode;

    @NotBlank(message = "Tên kích thước không được để trống")
    @Size(max = 150, message = "Tên kích thước không được quá 150 ký tự")
    private String sizeName;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String description;

    private Boolean isActive;
}
