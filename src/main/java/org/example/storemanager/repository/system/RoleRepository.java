package org.example.storemanager.repository.system;

import org.example.storemanager.entity.system.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByIdAndIsDeletedFalse(Long id);

    boolean existsByRoleName(String roleName);

    boolean existsByRoleNameAndIsDeletedFalse(String roleName);

    // Xử lý check duplicate khi update tên Role
    boolean existsByRoleNameAndIdNotAndIsDeletedFalse(String roleName, Long id);
}