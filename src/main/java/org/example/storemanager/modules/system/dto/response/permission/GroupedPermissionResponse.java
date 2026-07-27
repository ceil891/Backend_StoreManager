package org.example.storemanager.modules.system.dto.response.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupedPermissionResponse {
    private String module;
    private List<PermissionResponse> permissions;
}