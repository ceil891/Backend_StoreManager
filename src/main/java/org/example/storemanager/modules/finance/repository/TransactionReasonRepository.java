package org.example.storemanager.modules.finance.repository;

import org.example.storemanager.modules.finance.entity.TransactionReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface TransactionReasonRepository extends JpaRepository<TransactionReason, Long> {
    Optional<TransactionReason> findByIdAndIsDeletedFalse(Long id);
    List<TransactionReason> findByIsDeletedFalse();
}
