package org.example.storemanager.dto.request.catalog.inventory;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRejectRequest {
    @NotBlank(message = "Reject notes is required")
    private String rejectNotes;
}
