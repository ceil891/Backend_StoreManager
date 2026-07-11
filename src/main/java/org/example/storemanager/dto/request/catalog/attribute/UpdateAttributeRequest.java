package org.example.storemanager.dto.request.catalog.attribute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAttributeRequest {

    @NotBlank(message = "Tên thuộc tính không được để trống")
    @Size(max = 100, message = "Tên thuộc tính không được quá 100 ký tự")
    private String attributeName;

    @Size(max = 50, message = "Loại thuộc tính không được quá 50 ký tự")
    private String attributeType;
}
