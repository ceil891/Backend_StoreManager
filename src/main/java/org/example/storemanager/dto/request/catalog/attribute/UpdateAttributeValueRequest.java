package org.example.storemanager.dto.request.catalog.attribute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAttributeValueRequest {

    @NotBlank(message = "Giá trị không được để trống")
    @Size(max = 150, message = "Giá trị không được quá 150 ký tự")
    private String value;
}
