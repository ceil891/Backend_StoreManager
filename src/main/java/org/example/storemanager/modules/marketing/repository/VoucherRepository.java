package org.example.storemanager.modules.marketing.repository;

import org.example.storemanager.modules.marketing.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByIdAndIsDeletedFalse(Long id);
    List<Voucher> findByIsDeletedFalse();
    List<Voucher> findByIsDeletedFalseOrderByUpdatedAtDesc();
    Optional<Voucher> findByVoucherCode(String voucherCode);
}
