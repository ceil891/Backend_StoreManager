package org.example.storemanager.modules.system.dto.response.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UpdateUserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private Long roleId;
    private String roleName;
    private Long branchId;
    private String branchName;
    private LocalDateTime updatedAt;
    private String updatedBy;
}