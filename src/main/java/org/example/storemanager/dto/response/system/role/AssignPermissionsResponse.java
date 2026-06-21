package org.example.storemanager.dto.response.system.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionsResponse {
    private Long roleId;
    private List<Long> permissionIds;
    private String updatedBy;
    private LocalDateTime updatedAt;
}