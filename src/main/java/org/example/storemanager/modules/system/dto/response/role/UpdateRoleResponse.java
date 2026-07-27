package org.example.storemanager.modules.system.dto.response.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleResponse {
    private Long id;
    private String roleName;
    private Boolean isActive;
    private String description;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private java.util.List<String> permissions;
}