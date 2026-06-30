package org.example.storemanager.dto.response.system.systemconfig;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SystemConfigResponse {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private Boolean isActive; // Hoặc isDeleted tùy theo BaseEntity của bạn
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}