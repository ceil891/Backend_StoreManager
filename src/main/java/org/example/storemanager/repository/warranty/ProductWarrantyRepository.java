package org.example.storemanager.repository.warranty;

import org.example.storemanager.entity.warranty.ProductWarranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ProductWarrantyRepository extends JpaRepository<ProductWarranty, Long> {
    Optional<ProductWarranty> findByIdAndIsDeletedFalse(Long id);
    List<ProductWarranty> findByIsDeletedFalse();
}
