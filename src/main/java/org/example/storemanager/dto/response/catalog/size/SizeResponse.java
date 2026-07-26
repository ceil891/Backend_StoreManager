package org.example.storemanager.dto.response.catalog.size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeResponse {
    private Long id;
    private String sizeCode;
    private String sizeName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
