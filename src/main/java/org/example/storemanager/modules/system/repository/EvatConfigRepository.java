package org.example.storemanager.modules.system.repository;

import org.example.storemanager.modules.system.entity.EvatConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EvatConfigRepository extends JpaRepository<EvatConfig, Long> {
    Optional<EvatConfig> findByIdAndIsDeletedFalse(Long id);
    List<EvatConfig> findByIsDeletedFalse();
}
