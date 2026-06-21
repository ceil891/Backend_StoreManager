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
public class DeleteSizeResponse {
    private Long id;
    private String sizeCode;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
