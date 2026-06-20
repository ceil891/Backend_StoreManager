package org.example.storemanager.dto.response.system.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermissionResponse {
    private Long id;
    private String permissionCode;
    private String module;
}