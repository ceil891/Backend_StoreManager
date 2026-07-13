package org.example.storemanager.dto.response.system.systemconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteSystemConfigResponse {
    private Long id;
    private String configKey;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}