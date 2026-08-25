package org.example.storemanager.shared.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử AOP & Phân quyền Đa Chi Nhánh (Branch Scoping & Permission Test)")
class BranchScopeAspectTest {

    @BeforeEach
    void setUp() {
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("Test Case 1: User có quyền system:branch:view_all truyền branchId=2 -> Nhận đúng branchId=2")
    void testUserWithGlobalPermissionReceivesRequestedBranchId() {
        // Given: User có mã quyền system:branch:view_all và dataScope = "ALL"
        Set<String> permissions = new HashSet<>(Collections.singletonList("system:branch:view_all"));
        UserContext adminContext = UserContext.builder()
                .userId(1L)
                .username("superadmin")
                .branchId(1L)
                .dataScope("ALL")
                .permissions(permissions)
                .build();

        UserContextHolder.setContext(adminContext);

        // When: User truyền lên requestBranchId = 2
        Long requestedBranchId = 2L;
        Long effectiveBranchId = UserContextHolder.getEffectiveBranchId(requestedBranchId);

        // Then: Hệ thống giữ nguyên branchId = 2 được yêu cầu
        assertEquals(2L, effectiveBranchId, "User có quyền xem tất cả chi nhánh phải được phép truy vấn branchId=2");
    }

    @Test
    @DisplayName("Test Case 2: User không có quyền system:branch:view_all (Chi nhánh 1) truyền branchId=2 -> Hệ thống tự đè về branchId=1")
    void testUserWithoutGlobalPermissionIsRestrictedToAssignedBranchId() {
        // Given: User chỉ có quyền bán hàng cơ bản, làm việc tại Chi nhánh 1
        Set<String> permissions = new HashSet<>(Collections.singletonList("sales:order:create"));
        UserContext staffContext = UserContext.builder()
                .userId(2L)
                .username("staff_branch_1")
                .branchId(1L)
                .dataScope("BRANCH")
                .permissions(permissions)
                .build();

        UserContextHolder.setContext(staffContext);

        // When: User cố tình gửi requestBranchId = 2
        Long requestedBranchId = 2L;
        Long effectiveBranchId = UserContextHolder.getEffectiveBranchId(requestedBranchId);

        // Then: Hệ thống tự động đè về branchId = 1 của User, ngăn chặn Cross-Branch Data Leakage
        assertEquals(1L, effectiveBranchId, "User chi nhánh 1 khi truyền branchId=2 bắt buộc phải bị ghi đè về branchId=1");
    }

    @Test
    @DisplayName("Test Case 3: Kiểm thử khớp quyền động Wildcard (Dynamic Permission Wildcard Matching)")
    void testWildcardPermissionMatching() {
        Set<String> catalogAdminPermissions = new HashSet<>(Collections.singletonList("catalog:*"));
        assertTrue(SecurityEvaluator.matchPermission(catalogAdminPermissions, "catalog:product:create"));
        assertTrue(SecurityEvaluator.matchPermission(catalogAdminPermissions, "catalog:inventory:adjust"));
        assertFalse(SecurityEvaluator.matchPermission(catalogAdminPermissions, "sales:order:create"));

        Set<String> superAdminPermissions = new HashSet<>(Collections.singletonList("*"));
        assertTrue(SecurityEvaluator.matchPermission(superAdminPermissions, "sales:order:create"));
        assertTrue(SecurityEvaluator.matchPermission(superAdminPermissions, "system:user:delete"));
    }
}
