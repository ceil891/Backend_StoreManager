package org.example.storemanager.shared.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private Long userId;
    private String username;
    private Long branchId;
    @Builder.Default
    private String dataScope = "BRANCH"; // "ALL", "BRANCH", "PERSONAL"
    @Builder.Default
    private Set<String> roles = Collections.emptySet();
    @Builder.Default
    private Set<String> permissions = Collections.emptySet();

    /**
     * Kiểm tra user có quyền truy cập toàn bộ chi nhánh (Phạm vi ALL hoặc có mã quyền system:branch:view_all / *)
     */
    public boolean canViewAllBranches() {
        if ("ALL".equalsIgnoreCase(dataScope)) {
            return true;
        }
        if (permissions != null) {
            return permissions.contains("system:branch:view_all") ||
                   permissions.contains("branch:view_all") ||
                   permissions.contains("*") ||
                   permissions.contains("ALL");
        }
        return false;
    }

    public boolean isDataScopeAll() {
        return canViewAllBranches();
    }

    public boolean isDataScopeBranch() {
        return "BRANCH".equalsIgnoreCase(dataScope) && !canViewAllBranches();
    }

    public boolean isDataScopePersonal() {
        return "PERSONAL".equalsIgnoreCase(dataScope) && !canViewAllBranches();
    }
}
