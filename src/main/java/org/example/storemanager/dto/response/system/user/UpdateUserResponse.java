package org.example.storemanager.dto.response.system.user;

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
    private LocalDateTime updatedAt;
    private String updatedBy;
}