package org.example.storemanager.modules.finance.repository;

import org.example.storemanager.modules.finance.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByIdAndIsDeletedFalse(Long id);
    List<BankAccount> findByIsDeletedFalse();
}
