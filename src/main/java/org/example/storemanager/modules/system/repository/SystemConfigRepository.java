package org.example.storemanager.modules.system.repository;

import org.example.storemanager.modules.system.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    Optional<SystemConfig> findByIdAndIsDeletedFalse(Long id);
    List<SystemConfig> findByIsDeletedFalse();
}
