package org.example.storemanager.modules.catalog.dto.request.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferCompleteRequest {
    private String notes;
    private LocalDateTime completedAt;
}
