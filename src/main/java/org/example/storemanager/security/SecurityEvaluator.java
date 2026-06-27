package org.example.storemanager.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.entity.system.Role;
import org.example.storemanager.entity.system.RolePermission;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.repository.system.RolePermissionRepository;
import org.example.storemanager.repository.system.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component("securityEvaluator")
@RequiredArgsConstructor
public class SecurityEvaluator {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional(readOnly = true) // BẮT BUỘC CÓ ĐỂ TRÁNH LỖI LAZY LOAD CỦA HIBERNATE
    public boolean hasPermission(String requiredPermission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.warn("SecurityEvaluator: Chưa đăng nhập hoặc token không hợp lệ.");
            return false;
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null || Boolean.TRUE.equals(user.getIsDeleted()) || !"ACTIVE".equals(user.getStatus()) || user.getRole() == null) {
            log.warn("SecurityEvaluator: User lỗi trạng thái hoặc không có Role: {}", username);
            return false;
        }

        Role role = user.getRole();
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(role.getId());

        // ========================================================
        // KHU VỰC IN LOG RA MÀN HÌNH CONSOLE ĐỂ BẮT LỖI
        // ========================================================
        System.out.println("\n========= KIỂM TRA PHÂN QUYỀN API =========");
        System.out.println("1. Người gọi API: " + username);
        System.out.println("2. Vai trò (Role) trong DB: " + role.getRoleName() + " (ID: " + role.getId() + ")");
        System.out.println("3. Quyền API đang ĐÒI HỎI: [" + requiredPermission + "]");
        System.out.println("4. Danh sách quyền Role này ĐANG CÓ (" + rolePermissions.size() + " quyền):");

        boolean hasPerm = false;
        for (RolePermission rp : rolePermissions) {
            String currentCode = rp.getPermission().getPermissionCode();
            System.out.println("   - " + currentCode);
            if (currentCode.equals(requiredPermission)) {
                hasPerm = true;
            }
        }

        System.out.println("=> KẾT LUẬN: " + (hasPerm ? "CHO PHÉP ĐI QUA" : "BỊ TỪ CHỐI (403 FORBIDDEN)"));
        System.out.println("===========================================\n");
        // ========================================================

        return hasPerm;
    }
}