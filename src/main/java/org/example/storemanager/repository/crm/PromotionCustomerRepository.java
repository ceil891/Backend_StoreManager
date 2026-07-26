package org.example.storemanager.repository.crm;

import org.example.storemanager.entity.crm.PromotionCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PromotionCustomerRepository extends JpaRepository<PromotionCustomer, Long> {
    Optional<PromotionCustomer> findByIdAndIsDeletedFalse(Long id);
    List<PromotionCustomer> findByIsDeletedFalse();
}
