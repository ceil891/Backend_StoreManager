package org.example.storemanager.modules.catalog.repository;

import org.example.storemanager.modules.catalog.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(Long productId);
    Long countByProductIdAndIsApprovedTrue(Long productId);
}
