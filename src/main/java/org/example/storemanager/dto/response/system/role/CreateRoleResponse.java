package org.example.storemanager.dto.response.system.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleResponse {
    private Long id;
    private String roleName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
}