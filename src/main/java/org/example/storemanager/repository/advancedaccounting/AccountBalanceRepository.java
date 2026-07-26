package org.example.storemanager.repository.advancedaccounting;

import org.example.storemanager.entity.advancedaccounting.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {
    Optional<AccountBalance> findByIdAndIsDeletedFalse(Long id);
    List<AccountBalance> findByIsDeletedFalse();
}
