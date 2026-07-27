package org.example.storemanager.shared.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.system.entity.Role;
import org.example.storemanager.modules.system.entity.RolePermission;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.modules.system.repository.RolePermissionRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
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

        boolean hasPerm = rolePermissions.stream()
                .anyMatch(rp -> rp.getPermission() != null && requiredPermission.equals(rp.getPermission().getPermissionCode()));

        if (!hasPerm) {
            log.warn("Access Denied: User [{}] (Role: {}) missing permission [{}]", username, role.getRoleName(), requiredPermission);
        }

        return hasPerm;
    }
}
