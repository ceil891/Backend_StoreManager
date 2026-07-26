package org.example.storemanager.repository.finance;

import org.example.storemanager.entity.finance.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Optional<PaymentMethod> findByIdAndIsDeletedFalse(Long id);
    List<PaymentMethod> findByIsDeletedFalse();
}
