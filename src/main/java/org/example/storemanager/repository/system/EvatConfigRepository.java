package org.example.storemanager.repository.system;

import org.example.storemanager.entity.system.EvatConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EvatConfigRepository extends JpaRepository<EvatConfig, Long> {
    Optional<EvatConfig> findByIdAndIsDeletedFalse(Long id);
    List<EvatConfig> findByIsDeletedFalse();
}
