package org.example.storemanager.dto.response.system.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private Boolean isActive;

    private Long roleId;
    private String roleName;
    private Long branchId;
    private String branchName;

    private LocalDateTime createdAt;
    private Boolean isDeleted;
}