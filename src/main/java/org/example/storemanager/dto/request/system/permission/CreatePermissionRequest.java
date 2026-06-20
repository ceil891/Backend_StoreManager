package org.example.storemanager.dto.request.system.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePermissionRequest {
    @NotBlank(message = "Mã quyền không được để trống")
    @Size(max = 100, message = "Mã quyền không quá 100 ký tự")
    private String permissionCode;

    @NotBlank(message = "Tên module không được để trống")
    @Size(max = 50, message = "Tên module không quá 50 ký tự")
    private String module;

    private String description;
}