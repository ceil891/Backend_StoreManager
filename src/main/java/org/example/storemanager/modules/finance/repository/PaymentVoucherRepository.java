package org.example.storemanager.modules.finance.repository;

import org.example.storemanager.modules.finance.entity.PaymentVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PaymentVoucherRepository extends JpaRepository<PaymentVoucher, Long> {
    Optional<PaymentVoucher> findByIdAndIsDeletedFalse(Long id);
    List<PaymentVoucher> findByIsDeletedFalse();
}
