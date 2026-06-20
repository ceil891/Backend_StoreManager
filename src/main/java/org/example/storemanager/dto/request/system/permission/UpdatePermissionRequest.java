package org.example.storemanager.dto.request.system.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePermissionRequest {
    @NotBlank(message = "Tên module không được để trống")
    @Size(max = 50, message = "Tên module không quá 50 ký tự")
    private String module;

    private String description;
}