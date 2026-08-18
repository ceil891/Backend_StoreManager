package org.example.storemanager.modules.marketing.repository;

import org.example.storemanager.modules.marketing.entity.CustomerVoucher;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, Long> {
    Optional<CustomerVoucher> findByIdAndIsDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"customer", "voucher"})
    List<CustomerVoucher> findByIsDeletedFalse();

    @EntityGraph(attributePaths = {"customer", "voucher"})
    List<CustomerVoucher> findByIsDeletedFalseOrderByUpdatedAtDesc();
}
