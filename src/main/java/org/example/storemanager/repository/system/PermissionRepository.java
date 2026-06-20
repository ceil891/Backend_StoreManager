package org.example.storemanager.repository.system;

import org.example.storemanager.entity.system.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByPermissionCode(String permissionCode);
    List<Permission> findByIsDeletedFalse();
}