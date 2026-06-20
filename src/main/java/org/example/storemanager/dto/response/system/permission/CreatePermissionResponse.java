package org.example.storemanager.dto.response.system.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionResponse {
    private Long id;
    private String permissionCode;
    private String module;
    private LocalDateTime createdAt;
    private String createdBy;
}