package org.example.storemanager.repository.marketing;

import org.example.storemanager.entity.marketing.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByIdAndIsDeletedFalse(Long id);
    List<Voucher> findByIsDeletedFalse();
}
