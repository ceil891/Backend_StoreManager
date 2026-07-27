package org.example.storemanager.modules.finance.repository;

import org.example.storemanager.modules.finance.entity.ReceiptVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ReceiptVoucherRepository extends JpaRepository<ReceiptVoucher, Long> {
    Optional<ReceiptVoucher> findByIdAndIsDeletedFalse(Long id);
    List<ReceiptVoucher> findByIsDeletedFalse();
}
