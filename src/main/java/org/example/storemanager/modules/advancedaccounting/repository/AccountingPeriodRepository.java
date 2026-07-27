package org.example.storemanager.modules.advancedaccounting.repository;

import org.example.storemanager.modules.advancedaccounting.entity.AccountingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, Long> {
    Optional<AccountingPeriod> findByIdAndIsDeletedFalse(Long id);
    List<AccountingPeriod> findByIsDeletedFalse();
}
