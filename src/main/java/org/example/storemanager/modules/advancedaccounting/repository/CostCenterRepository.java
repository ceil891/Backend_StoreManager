package org.example.storemanager.modules.advancedaccounting.repository;

import org.example.storemanager.modules.advancedaccounting.entity.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, Long> {
    Optional<CostCenter> findByIdAndIsDeletedFalse(Long id);
    List<CostCenter> findByIsDeletedFalse();
}
