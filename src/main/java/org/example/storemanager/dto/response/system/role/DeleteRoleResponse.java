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
public class DeleteRoleResponse {
    private Long id;
    private String roleName;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private String deletionReason;
}