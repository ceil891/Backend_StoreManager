package org.example.storemanager.modules.advancedaccounting.repository;

import org.example.storemanager.modules.advancedaccounting.entity.ChartOfAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ChartOfAccountRepository extends JpaRepository<ChartOfAccount, Long> {
    Optional<ChartOfAccount> findByIdAndIsDeletedFalse(Long id);
    List<ChartOfAccount> findByIsDeletedFalse();
    Optional<ChartOfAccount> findByAccountCodeAndIsDeletedFalse(String accountCode);
}
