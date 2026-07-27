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
public class DeleteColorResponse {
    private Long id;
    private String colorCode;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
