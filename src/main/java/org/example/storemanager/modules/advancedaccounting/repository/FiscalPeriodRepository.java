package org.example.storemanager.modules.advancedaccounting.repository;

import org.example.storemanager.modules.advancedaccounting.entity.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, Long> {
    Optional<FiscalPeriod> findByIdAndIsDeletedFalse(Long id);
    List<FiscalPeriod> findByIsDeletedFalse();
}
