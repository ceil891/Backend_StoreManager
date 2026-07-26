package org.example.storemanager.repository.crm;

import org.example.storemanager.entity.crm.PromotionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PromotionCategoryRepository extends JpaRepository<PromotionCategory, Long> {
    Optional<PromotionCategory> findByIdAndIsDeletedFalse(Long id);
    List<PromotionCategory> findByIsDeletedFalse();
}
