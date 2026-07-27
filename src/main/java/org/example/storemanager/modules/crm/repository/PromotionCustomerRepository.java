package org.example.storemanager.modules.crm.repository;

import org.example.storemanager.modules.crm.entity.PromotionCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PromotionCustomerRepository extends JpaRepository<PromotionCustomer, Long> {
    Optional<PromotionCustomer> findByIdAndIsDeletedFalse(Long id);
    List<PromotionCustomer> findByIsDeletedFalse();
}
