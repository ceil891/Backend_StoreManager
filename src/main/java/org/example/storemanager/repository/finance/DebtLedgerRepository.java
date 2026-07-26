package org.example.storemanager.repository.finance;

import org.example.storemanager.entity.finance.DebtLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DebtLedgerRepository extends JpaRepository<DebtLedger, Long> {
    Optional<DebtLedger> findByIdAndIsDeletedFalse(Long id);
    List<DebtLedger> findByIsDeletedFalse();
}
