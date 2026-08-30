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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component("securityEvaluator")
@RequiredArgsConstructor
public class SecurityEvaluator {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;

    // Cache permissions per roleId với TTL (in-memory fast cache tối ưu response time < 50ms)
    private static final Map<Long, CachedRolePermissions> ROLE_PERMISSIONS_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, CachedUserInfo> USER_INFO_CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 phút
    private static final long USER_CACHE_TTL_MS = 3 * 60 * 1000; // 3 phút

    private static class CachedRolePermissions {
        final Set<String> permissions;
        final long cachedAt;

        CachedRolePermissions(Set<String> permissions) {
            this.permissions = permissions;
            this.cachedAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > CACHE_TTL_MS;
        }
    }

    private static class CachedUserInfo {
        final Long userId;
        final String status;
        final Boolean isDeleted;
        final Long roleId;
        final String roleName;
        final long cachedAt;

        CachedUserInfo(Long userId, String status, Boolean isDeleted, Long roleId, String roleName) {
            this.userId = userId;
            this.status = status;
            this.isDeleted = isDeleted;
            this.roleId = roleId;
            this.roleName = roleName;
            this.cachedAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > USER_CACHE_TTL_MS;
        }
    }

    /**
     * Xóa cache khi cập nhật phân quyền cho Role
     */
    public static void evictRoleCache(Long roleId) {
        if (roleId != null) {
            ROLE_PERMISSIONS_CACHE.remove(roleId);
        } else {
            ROLE_PERMISSIONS_CACHE.clear();
        }
    }

    /**
     * Xóa cache User khi user bị thay đổi trạng thái, quyền, hoặc thông tin
     */
    public static void evictUserCache(String usernameOrEmail) {
        if (usernameOrEmail != null && !usernameOrEmail.trim().isEmpty()) {
            USER_INFO_CACHE.remove(usernameOrEmail.trim().toLowerCase());
        } else {
            USER_INFO_CACHE.clear();
        }
    }

    public static void evictAll() {
        ROLE_PERMISSIONS_CACHE.clear();
        USER_INFO_CACHE.clear();
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(String requiredPermission) {
        if (requiredPermission == null || requiredPermission.trim().isEmpty()) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.debug("SecurityEvaluator: Cho phép truy cập chế độ tự do (Anonymous/Dev)");
            return true;
        }

        String principal = auth.getName();
        String userKey = principal.toLowerCase().trim();
        CachedUserInfo cachedUser = USER_INFO_CACHE.get(userKey);

        if (cachedUser == null || cachedUser.isExpired()) {
            User user = userRepository.findByUsername(principal)
                    .or(() -> userRepository.findByEmail(principal))
                    .orElse(null);

            if (user == null || Boolean.TRUE.equals(user.getIsDeleted()) || !"ACTIVE".equals(user.getStatus()) || user.getRole() == null) {
                log.warn("SecurityEvaluator: User không hợp lệ hoặc đã bị vô hiệu hóa: {}", principal);
                USER_INFO_CACHE.remove(userKey);
                return false;
            }

            cachedUser = new CachedUserInfo(
                    user.getId(),
                    user.getStatus(),
                    user.getIsDeleted(),
                    user.getRole().getId(),
                    user.getRole().getRoleName()
            );
            USER_INFO_CACHE.put(userKey, cachedUser);
            if (user.getUsername() != null) USER_INFO_CACHE.put(user.getUsername().toLowerCase().trim(), cachedUser);
            if (user.getEmail() != null) USER_INFO_CACHE.put(user.getEmail().toLowerCase().trim(), cachedUser);
        }

        if (Boolean.TRUE.equals(cachedUser.isDeleted) || !"ACTIVE".equals(cachedUser.status) || cachedUser.roleId == null) {
            log.warn("SecurityEvaluator: User không hợp lệ hoặc đã bị vô hiệu hóa: {}", principal);
            return false;
        }

        if ("SUPER_ADMIN".equalsIgnoreCase(cachedUser.roleName)) {
            return true;
        }

        Set<String> permissions = getPermissionsForRole(cachedUser.roleId, cachedUser.roleName);

        // Kiểm tra quyền wildcard hoặc match chính xác
        boolean hasPerm = matchPermission(permissions, requiredPermission);

        if (!hasPerm) {
            log.warn("Access Denied: User [{}] (Role: {}) missing permission [{}]", principal, cachedUser.roleName, requiredPermission);
        }

        return hasPerm;
    }

    @Transactional(readOnly = true)
    public boolean hasAnyPermission(String... requiredPermissions) {
        if (requiredPermissions == null || requiredPermissions.length == 0) return true;
        for (String perm : requiredPermissions) {
            if (hasPermission(perm)) return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean hasAllPermissions(String... requiredPermissions) {
        if (requiredPermissions == null || requiredPermissions.length == 0) return true;
        for (String perm : requiredPermissions) {
            if (!hasPermission(perm)) return false;
        }
        return true;
    }

    @Transactional(readOnly = true)
    public boolean hasDataScope(String requiredScope) {
        UserContext ctx = UserContextHolder.getContext();
        if (ctx == null) return true;
        if ("ALL".equalsIgnoreCase(requiredScope)) {
            return ctx.canViewAllBranches();
        }
        return true;
    }

    private Set<String> getPermissionsForRole(Long roleId, String roleName) {
        CachedRolePermissions cached = ROLE_PERMISSIONS_CACHE.get(roleId);
        if (cached != null && !cached.isExpired()) {
            return cached.permissions;
        }

        Set<String> perms = new HashSet<>();
        if ("SUPER_ADMIN".equalsIgnoreCase(roleName)) {
            perms.add("*");
            perms.add("ALL");
        }

        Set<String> dbPerms = rolePermissionRepository.findPermissionCodesByRoleId(roleId);
        if (dbPerms != null) {
            for (String code : dbPerms) {
                if (code != null && !code.trim().isEmpty()) {
                    perms.add(code.trim());
                }
            }
        }

        ROLE_PERMISSIONS_CACHE.put(roleId, new CachedRolePermissions(perms));
        return perms;
    }

    public static boolean matchPermission(Set<String> userPermissions, String requiredPermission) {
        if (userPermissions == null || userPermissions.isEmpty()) return false;
        if (userPermissions.contains("*") || userPermissions.contains("ALL")) return true;
        if (userPermissions.contains(requiredPermission)) return true;

        // Dynamic Wildcard matching: e.g. catalog:inventory:view matches catalog:* or catalog:inventory:*
        String[] parts = requiredPermission.split(":");
        if (parts.length >= 2) {
            String domainWildcard = parts[0] + ":*";
            String resourceWildcard = parts[0] + ":" + parts[1] + ":*";
            if (userPermissions.contains(domainWildcard) || userPermissions.contains(resourceWildcard)) {
                return true;
            }
        }
        return false;
    }
}
