package org.example.storemanager.dto.request.system.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(max = 50, message = "Tên vai trò không quá 50 ký tự")
    private String roleName;

    private String description;
    private Boolean isActive;
}