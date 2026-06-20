package org.example.storemanager.dto.response.system.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreateUserResponse {
    private Long id;
    private String username;
    private String fullName;
    private LocalDateTime createdAt;
    private String createdBy;
}