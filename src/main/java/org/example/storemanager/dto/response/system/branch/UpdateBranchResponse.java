package org.example.storemanager.dto.response.system.branch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBranchResponse {
    private Long id;
    private String branchCode;
    private String branchName;
    private String address;
    private String phone;
    private Boolean isActive;
    private Long managerId;
    private String managerName;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
