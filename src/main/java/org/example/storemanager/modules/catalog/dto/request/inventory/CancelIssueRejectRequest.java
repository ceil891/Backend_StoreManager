package org.example.storemanager.modules.catalog.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelIssueRejectRequest {
    @NotBlank(message = "Reject notes is required")
    private String rejectNotes;
}
