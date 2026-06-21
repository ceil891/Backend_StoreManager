package org.example.storemanager.dto.response.system.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeleteUserResponse {
    private Long id;
    private String username;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}