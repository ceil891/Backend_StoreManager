package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {
    @Query("SELECT b FROM ProductBatch b LEFT JOIN FETCH b.product WHERE b.isDeleted = false")
    List<ProductBatch> findAllWithAssociations();

    Optional<ProductBatch> findByIdAndIsDeletedFalse(Long id);
}
