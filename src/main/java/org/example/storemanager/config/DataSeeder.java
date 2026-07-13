package org.example.storemanager.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.entity.system.Permission;
import org.example.storemanager.entity.system.Role;
import org.example.storemanager.entity.system.RolePermission;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.repository.system.PermissionRepository;
import org.example.storemanager.repository.system.RolePermissionRepository;
import org.example.storemanager.repository.system.RoleRepository;
import org.example.storemanager.repository.system.UserRepository;
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

    // Pattern Regex trích xuất tham số của hasPermission('xxx')
    private static final Pattern PERMISSION_PATTERN = Pattern.compile("hasPermission\\s*\\(\\s*'([^']+)'\\s*\\)");

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("======= BẮT ĐẦU QUÉT PHÂN QUYỀN ĐỘNG (DYNAMIC PERMISSION SCANNER) =======");

        // 1. Quét tất cả các RestControllers trong ứng dụng để thu thập các mã quyền từ @PreAuthorize
        Set<String> scannedPermissionCodes = scanControllerPermissions();
        log.info("Đã quét được tổng cộng {} mã quyền độc nhất từ các Controller.", scannedPermissionCodes.size());

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
            adminUser.setIsDeleted(false);

            userRepository.save(adminUser);
            log.info("Đã tạo tài khoản 'admin' mặc định.");
        }

        log.info("======= HOÀN THÀNH QUÉT PHÂN QUYỀN ĐỘNG =======");
    }


    private Set<String> scanControllerPermissions() {
        Set<String> permissionCodes = new HashSet<>();
        Map<String, Object> controllerBeans = applicationContext.getBeansWithAnnotation(RestController.class);

        for (Object bean : controllerBeans.values()) {
            // Lấy class thực tế (tránh bị ảnh hưởng bởi Spring CGLIB Proxy)
            Class<?> controllerClass = bean.getClass();
            if (controllerClass.getName().contains("$$")) {
                controllerClass = controllerClass.getSuperclass();
            }

            // A. Quét @PreAuthorize trên cấp Class (Controller level)
            if (controllerClass.isAnnotationPresent(PreAuthorize.class)) {
                PreAuthorize preAuthorize = controllerClass.getAnnotation(PreAuthorize.class);
                extractPermissions(preAuthorize.value(), permissionCodes);
            }

            // B. Quét @PreAuthorize trên từng Method
            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PreAuthorize.class)) {
                    PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
                    extractPermissions(preAuthorize.value(), permissionCodes);
                }
            }
        }

        return permissionCodes;
    }


    private void extractPermissions(String expression, Set<String> permissionCodes) {
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }
        Matcher matcher = PERMISSION_PATTERN.matcher(expression);
        while (matcher.find()) {
            String code = matcher.group(1);
            if (code != null && !code.trim().isEmpty()) {
                permissionCodes.add(code.trim());
            }
        }
    }


    private String determineModuleFromCode(String code) {
        if (code.contains(":")) {
            String prefix = code.split(":")[0];
            // Viết hoa chữ cái đầu cho đẹp
            return prefix.substring(0, 1).toUpperCase() + prefix.substring(1).toLowerCase();
        }
        return "General";
    }
}