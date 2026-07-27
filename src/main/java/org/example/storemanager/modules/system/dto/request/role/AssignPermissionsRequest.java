package org.example.storemanager.modules.system.dto.request.role;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class AssignPermissionsRequest {
    @NotEmpty(message = "Danh sách ID quyền không được để trống")
    private List<Long> permissionIds;
}