package org.example.storemanager.modules.crm.repository;

import org.example.storemanager.modules.crm.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByIdAndIsDeletedFalse(Long id);
    List<Promotion> findByIsDeletedFalse();
}
