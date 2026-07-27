package org.example.storemanager.modules.catalog.dto.response.color;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateColorResponse {
    private Long id;
    private String colorCode;
    private String colorName;
    private String hexValue;
    private String description;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
