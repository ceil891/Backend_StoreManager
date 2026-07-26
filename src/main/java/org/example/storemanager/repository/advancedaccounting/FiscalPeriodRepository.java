package org.example.storemanager.repository.advancedaccounting;

import org.example.storemanager.entity.advancedaccounting.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, Long> {
    Optional<FiscalPeriod> findByIdAndIsDeletedFalse(Long id);
    List<FiscalPeriod> findByIsDeletedFalse();
}
