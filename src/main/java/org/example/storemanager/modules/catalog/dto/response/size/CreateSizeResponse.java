package org.example.storemanager.modules.catalog.dto.response.size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSizeResponse {
    private Long id;
    private String sizeCode;
    private String sizeName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
}
