package org.example.storemanager.modules.system.repository;

import org.example.storemanager.modules.system.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);

    void deleteByRoleIdAndPermissionIdIn(Long roleId, List<Long> permissionIds);
}