package org.example.storemanager.config;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.entity.system.Permission;
import org.example.storemanager.entity.system.Role;
import org.example.storemanager.entity.system.RolePermission;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.repository.system.PermissionRepository;
import org.example.storemanager.repository.system.RolePermissionRepository;
import org.example.storemanager.repository.system.RoleRepository;
import org.example.storemanager.repository.system.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Tự động sinh danh sách Core Permissions nếu chưa có trong DB
        List<Permission> pendingPermissions = new ArrayList<>();

        // Quyền hạn module User
        addIfAbsent("system:user:view", "System", "Xem thông tin người dùng", pendingPermissions);
        addIfAbsent("system:user:create", "System", "Tạo mới người dùng", pendingPermissions);
        addIfAbsent("system:user:update", "System", "Cập nhật người dùng", pendingPermissions);
        addIfAbsent("system:user:delete", "System", "Xóa người dùng", pendingPermissions);

        // Quyền hạn module Role
        addIfAbsent("system:role:view", "System", "Xem danh sách vai trò", pendingPermissions);
        addIfAbsent("system:role:create", "System", "Tạo mới vai trò", pendingPermissions);
        addIfAbsent("system:role:update", "System", "Cập nhật vai trò", pendingPermissions);
        addIfAbsent("system:role:delete", "System", "Xóa vai trò", pendingPermissions);
        addIfAbsent("system:role:assign-permissions", "System", "Gán quyền cho vai trò", pendingPermissions);

        // Quyền hạn module Permission
        addIfAbsent("system:permission:view", "System", "Xem danh sách quyền hạn", pendingPermissions);
        addIfAbsent("system:permission:create", "System", "Tạo mới quyền hạn", pendingPermissions);
        addIfAbsent("system:permission:update", "System", "Cập nhật quyền hạn", pendingPermissions);
        addIfAbsent("system:permission:delete", "System", "Xóa quyền hạn", pendingPermissions);

        if (!pendingPermissions.isEmpty()) {
            permissionRepository.saveAll(pendingPermissions);
        }

        // 2. Khởi tạo thực thể SUPER_ADMIN và tự động liên kết toàn bộ quyền hệ thống
        Role superAdminRole = null;
        if (!roleRepository.existsByRoleName("SUPER_ADMIN")) {
            superAdminRole = Role.builder()
                    .roleName("SUPER_ADMIN")
                    .description("Vai trò quản trị tối cao của toàn bộ hệ thống")
                    .isActive(true)
                    .build();
            // Dùng setter thay vì builder cho các trường của BaseEntity
            superAdminRole.setIsDeleted(false);

            superAdminRole = roleRepository.save(superAdminRole);

            List<Permission> allPermissions = permissionRepository.findAll();
            List<RolePermission> defaultAssignments = new ArrayList<>();

            for (Permission perm : allPermissions) {
                defaultAssignments.add(RolePermission.builder()
                        .role(superAdminRole)
                        .permission(perm)
                        .build());
            }
            rolePermissionRepository.saveAll(defaultAssignments);
        } else {
            // Lấy role ra nếu đã tồn tại để gán cho User
            superAdminRole = roleRepository.findByRoleName("SUPER_ADMIN").orElse(null);
        }

        // 3. TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH
        if (superAdminRole != null && !userRepository.existsByUsername("admin")) {
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản trị viên hệ thống")
                    .email("admin@storemanager.com")
                    .phone("0999999999")
                    .status("ACTIVE")
                    .role(superAdminRole)
                    .build();
            // Dùng setter thay vì builder cho trường kế thừa
            adminUser.setIsDeleted(false);

            userRepository.save(adminUser);
        }
    }

    private void addIfAbsent(String code, String module, String desc, List<Permission> list) {
        if (!permissionRepository.existsByPermissionCode(code)) {
            Permission perm = Permission.builder()
                    .permissionCode(code)
                    .module(module)
                    .description(desc)
                    .build();
            // Dùng setter thay vì builder
            perm.setIsDeleted(false);
            list.add(perm);
        }
    }
}