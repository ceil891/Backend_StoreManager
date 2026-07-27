package org.example.storemanager.modules.system.dto.response.branch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
    private Long id;
    private String branchCode;
    private String branchName;
    private String address;
    private String phone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private BranchManagerResponse manager;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchManagerResponse {
        private Long id;
        private String username;
        private String fullName;
    }
}
