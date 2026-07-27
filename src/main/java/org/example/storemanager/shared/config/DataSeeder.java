package org.example.storemanager.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.system.entity.Permission;
import org.example.storemanager.modules.system.entity.Role;
import org.example.storemanager.modules.system.entity.RolePermission;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.modules.system.repository.PermissionRepository;
import org.example.storemanager.modules.system.repository.RolePermissionRepository;
import org.example.storemanager.modules.system.repository.RoleRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationContext applicationContext;

    private static final Pattern PERMISSION_PATTERN = Pattern.compile("hasPermission\\s*\\(\\s*'([^']+)'\\s*\\)");

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 1. Quét tất cả các RestControllers trong ứng dụng để thu thập các mã quyền từ @PreAuthorize
        Set<String> scannedPermissionCodes = scanControllerPermissions();

        // 2. Thêm các quyền mới phát hiện vào DB nếu chưa tồn tại
        List<Permission> pendingPermissions = new ArrayList<>();
        for (String code : scannedPermissionCodes) {
            if (!permissionRepository.existsByPermissionCode(code)) {
                // Xác định tên Module và mô tả cơ bản từ mã quyền (ví dụ: "catalog:product:create" -> Module "Catalog")
                String moduleName = determineModuleFromCode(code);
                String description = "Quyền truy cập chức năng " + code;

                Permission perm = Permission.builder()
                        .permissionCode(code)
                        .module(moduleName)
                        .description(description)
                        .isActive(true)
                        .build();
                perm.setIsDeleted(false);
                pendingPermissions.add(perm);
                log.info("Phát hiện quyền mới -> chuẩn bị lưu: [{}] - Module: [{}]", code, moduleName);
            }
        }

        if (!pendingPermissions.isEmpty()) {
            permissionRepository.saveAllAndFlush(pendingPermissions);
            log.info("Đã lưu thành công {} quyền mới vào Database.", pendingPermissions.size());
        }

        // 3. Khởi tạo thực thể SUPER_ADMIN (nếu chưa có)
        Role superAdminRole = roleRepository.findByRoleName("SUPER_ADMIN").orElse(null);

        if (superAdminRole == null) {
            superAdminRole = Role.builder()
                    .roleName("SUPER_ADMIN")
                    .description("Vai trò quản trị tối cao của toàn bộ hệ thống")
                    .isActive(true)
                    .build();
            superAdminRole.setIsDeleted(false);
            superAdminRole = roleRepository.saveAndFlush(superAdminRole);
            log.info("Đã tạo mới vai trò SUPER_ADMIN mặc định.");
        }

        // 4. CẬP NHẬT TỰ ĐỘNG CÁ C QUYỀN MỚI NHẤT CHO SUPER_ADMIN
        List<Permission> allPermissions = permissionRepository.findAll();
        List<RolePermission> currentRolePermissions = rolePermissionRepository.findByRoleId(superAdminRole.getId());
        Set<Long> currentPermissionIds = currentRolePermissions.stream()
                .map(rp -> rp.getPermission().getId())
                .collect(Collectors.toSet());

        List<RolePermission> newPermissionsToAssign = new ArrayList<>();
        for (Permission perm : allPermissions) {
            if (!currentPermissionIds.contains(perm.getId())) {
                RolePermission newRp = RolePermission.builder()
                        .role(superAdminRole)
                        .permission(perm)
                        .build();
                newRp.setIsDeleted(false);
                newPermissionsToAssign.add(newRp);
            }
        }

        if (!newPermissionsToAssign.isEmpty()) {
            rolePermissionRepository.saveAllAndFlush(newPermissionsToAssign);
            log.info("Đã gán bổ sung {} quyền mới cho vai trò SUPER_ADMIN.", newPermissionsToAssign.size());
        }

        // 5. TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản trị viên hệ thống")
                    .email("admin@storemanager.com")
                    .phone("0999999999")
                    .status("ACTIVE")
                    .role(superAdminRole)
                    .build();
            userRepository.save(adminUser);
            log.info("Đã tạo tài khoản 'admin' mặc định.");
        }

    }

    /**
     * Quét tất cả các @RestController bean trong ApplicationContext,
     * tìm tất cả method có @PreAuthorize("hasPermission('...')") và trích xuất mã quyền.
     */
    private Set<String> scanControllerPermissions() {
        Set<String> permissionCodes = new LinkedHashSet<>();
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);

        for (Object bean : controllers.values()) {
            Class<?> targetClass = bean.getClass();
            // Unwrap CGLIB proxy nếu có
            while (targetClass.getSimpleName().contains("$$")) {
                targetClass = targetClass.getSuperclass();
            }

            for (Method method : targetClass.getDeclaredMethods()) {
                PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
                if (preAuthorize != null) {
                    String expression = preAuthorize.value();
                    Matcher matcher = PERMISSION_PATTERN.matcher(expression);
                    while (matcher.find()) {
                        String permCode = matcher.group(1).trim();
                        if (!permCode.isEmpty()) {
                            permissionCodes.add(permCode);
                        }
                    }
                }
            }
        }
        return permissionCodes;
    }

    /**
     * Xác định tên Module từ mã quyền.
     * Ví dụ: "catalog:product:create" -> "Catalog"
     *         "inventory:import:approve" -> "Inventory"
     */
    private String determineModuleFromCode(String code) {
        if (code == null || code.isBlank()) return "General";
        String[] parts = code.split("[:_\\-]");
        if (parts.length == 0) return "General";

        String prefix = parts[0].toLowerCase();
        return switch (prefix) {
            case "catalog"          -> "Catalog - Danh mục sản phẩm";
            case "inventory"        -> "Inventory - Kho hàng";
            case "purchase"         -> "Purchase - Mua hàng";
            case "sales"            -> "Sales - Bán hàng";
            case "crm"              -> "CRM - Khách hàng";
            case "hrm"              -> "HRM - Nhân sự";
            case "finance"          -> "Finance - Tài chính";
            case "wms"              -> "WMS - Quản lý kho vật lý";
            case "system"           -> "System - Hệ thống";
            case "partnerarea"      -> "Partner Area - Đối tác";
            case "report"           -> "Report - Báo cáo";
            case "omnichannel"      -> "Omnichannel - Đa kênh";
            default                 -> capitalize(prefix);
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
