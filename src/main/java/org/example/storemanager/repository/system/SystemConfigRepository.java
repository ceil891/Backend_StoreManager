package org.example.storemanager.repository.system;

import org.example.storemanager.entity.system.SystemConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long>, JpaSpecificationExecutor<SystemConfig> {
    Optional<SystemConfig> findByConfigKey(String configKey);
    boolean existsByConfigKey(String configKey);
}