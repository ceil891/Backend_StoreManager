package org.example.storemanager.dto.response.system.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserResponse {
    private Long id;
    private String username;
    private String status;
}