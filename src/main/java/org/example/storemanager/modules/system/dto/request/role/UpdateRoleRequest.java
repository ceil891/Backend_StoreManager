package org.example.storemanager.modules.system.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class UpdateRoleRequest {
    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(max = 50, message = "Tên vai trò không quá 50 ký tự")
    private String roleName;

    private String description;
    private Boolean isActive;
    private List<String> grantedPermissions;
    private List<Long> permissionIds;
}