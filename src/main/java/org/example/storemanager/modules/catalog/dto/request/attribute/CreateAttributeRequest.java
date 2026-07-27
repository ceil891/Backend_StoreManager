package org.example.storemanager.modules.catalog.dto.request.attribute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAttributeRequest {

    @NotBlank(message = "Tên thuộc tính không được để trống")
    @Size(max = 100, message = "Tên thuộc tính không được quá 100 ký tự")
    private String attributeName;

    @NotBlank(message = "Mã thuộc tính không được để trống")
    @Size(max = 50, message = "Mã thuộc tính không được quá 50 ký tự")
    private String attributeCode;

    @Size(max = 50, message = "Loại thuộc tính không được quá 50 ký tự")
    private String attributeType;
}
