package org.example.storemanager.modules.catalog.dto.request.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDepartmentRequest {

    @NotBlank(message = "Mã ngành hàng không được để trống")
    @Size(max = 50, message = "Mã ngành hàng không được quá 50 ký tự")
    private String deptCode;

    @NotBlank(message = "Tên ngành hàng không được để trống")
    @Size(max = 150, message = "Tên ngành hàng không được quá 150 ký tự")
    private String deptName;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String description;

    private Boolean isActive;
}
