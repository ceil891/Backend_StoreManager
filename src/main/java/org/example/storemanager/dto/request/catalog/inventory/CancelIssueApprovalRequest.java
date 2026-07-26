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
public class CancelIssueApprovalRequest {
    @NotBlank(message = "Approval notes is required")
    private String approvalNotes;
}
