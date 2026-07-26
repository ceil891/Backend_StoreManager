package org.example.storemanager.repository.finance;

import org.example.storemanager.entity.finance.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
    Optional<OrderPayment> findByIdAndIsDeletedFalse(Long id);
    List<OrderPayment> findByIsDeletedFalse();
}
