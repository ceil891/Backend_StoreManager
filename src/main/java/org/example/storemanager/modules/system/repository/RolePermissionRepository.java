package org.example.storemanager.modules.system.repository;

import org.example.storemanager.modules.system.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);

    @Query("SELECT rp FROM RolePermission rp JOIN FETCH rp.permission WHERE rp.role.id = :roleId")
    List<RolePermission> findByRoleIdWithPermission(@Param("roleId") Long roleId);

    @Query("SELECT p.permissionCode FROM RolePermission rp JOIN rp.permission p WHERE rp.role.id = :roleId AND (p.isActive = true OR p.isActive IS NULL) AND (p.isDeleted = false OR p.isDeleted IS NULL)")
    java.util.Set<String> findPermissionCodesByRoleId(@Param("roleId") Long roleId);

    void deleteByRoleId(Long roleId);

    void deleteByRoleIdAndPermissionIdIn(Long roleId, List<Long> permissionIds);
}