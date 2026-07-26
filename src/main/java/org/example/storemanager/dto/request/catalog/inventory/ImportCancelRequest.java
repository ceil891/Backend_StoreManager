package org.example.storemanager.dto.request.catalog.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportCancelRequest {
    @NotBlank(message = "Cancel reason is required")
    @Size(min = 10, max = 500, message = "Reason must be 10-500 characters")
    private String cancelReason;
}
