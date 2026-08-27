package org.example.storemanager.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.storemanager.modules.system.entity.Role;
import org.example.storemanager.modules.system.entity.RolePermission;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.modules.system.repository.RolePermissionRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.example.storemanager.shared.annotation.BranchScoped;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring AOP Aspect chặn các Controller / Service có đánh dấu @BranchScoped
 * Tự động đồng bộ quyền hạn và thiết lập branchId vào UserContextHolder.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class BranchScopeAspect {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Around("@within(branchScoped) || @annotation(branchScoped)")
    public Object applyBranchScoping(ProceedingJoinPoint joinPoint, BranchScoped branchScoped) throws Throwable {
        try {
            resolveAndPopulateUserContext();
            return joinPoint.proceed();
        } finally {
            // Context sẽ được dọn dẹp sau khi request kết thúc
        }
    }

    private void resolveAndPopulateUserContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;

        Long headerBranchId = null;
        if (request != null) {
            String headerVal = request.getHeader("X-Branch-Id");
            if (headerVal != null && !headerVal.trim().isEmpty()) {
                try {
                    headerBranchId = Long.parseLong(headerVal.trim());
                } catch (NumberFormatException ignored) {}
            }
        }

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            UserContext ctx = UserContext.builder()
                    .username("anonymous")
                    .branchId(headerBranchId)
                    .roles(Collections.emptySet())
                    .permissions(Collections.emptySet())
                    .build();
            UserContextHolder.setContext(ctx);
            return;
        }

        String principal = auth.getName();
        User user = userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElse(null);

        Long userBranchId = user != null && user.getBranch() != null ? user.getBranch().getId() : headerBranchId;

        Set<String> roleCodes = new HashSet<>();
        Set<String> permissionCodes = new HashSet<>();

        if (user != null && user.getRole() != null) {
            Role role = user.getRole();
            if (role.getRoleName() != null) {
                roleCodes.add(role.getRoleName());
            }

            if ("SUPER_ADMIN".equalsIgnoreCase(role.getRoleName())) {
                permissionCodes.add("*");
                permissionCodes.add("ALL");
            }

            Set<String> dbPerms = rolePermissionRepository.findPermissionCodesByRoleId(role.getId());
            if (dbPerms != null) {
                permissionCodes.addAll(dbPerms);
            }
        }

        UserContext ctx = UserContext.builder()
                .userId(user != null ? user.getId() : null)
                .username(principal)
                .branchId(userBranchId)
                .roles(roleCodes)
                .permissions(permissionCodes)
                .build();

        UserContextHolder.setContext(ctx);
    }
}
