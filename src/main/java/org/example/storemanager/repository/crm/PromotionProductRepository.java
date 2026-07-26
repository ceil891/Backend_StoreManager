package org.example.storemanager.repository.crm;

import org.example.storemanager.entity.crm.PromotionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long> {
    Optional<PromotionProduct> findByIdAndIsDeletedFalse(Long id);
    List<PromotionProduct> findByIsDeletedFalse();
}
