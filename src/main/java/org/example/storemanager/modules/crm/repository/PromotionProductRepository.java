package org.example.storemanager.modules.crm.repository;

import org.example.storemanager.modules.crm.entity.PromotionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long> {
    Optional<PromotionProduct> findByIdAndIsDeletedFalse(Long id);
    List<PromotionProduct> findByIsDeletedFalse();
}
