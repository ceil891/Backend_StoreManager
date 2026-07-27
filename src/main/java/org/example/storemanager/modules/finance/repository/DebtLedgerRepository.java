package org.example.storemanager.modules.finance.repository;

import org.example.storemanager.modules.finance.entity.DebtLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DebtLedgerRepository extends JpaRepository<DebtLedger, Long> {
    Optional<DebtLedger> findByIdAndIsDeletedFalse(Long id);
    List<DebtLedger> findByIsDeletedFalse();
}
