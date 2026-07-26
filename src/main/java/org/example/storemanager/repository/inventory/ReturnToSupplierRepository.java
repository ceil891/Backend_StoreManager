package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.ReturnToSupplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnToSupplierRepository extends JpaRepository<ReturnToSupplier, Long> {
    @Query("SELECT r FROM ReturnToSupplier r LEFT JOIN FETCH r.branch LEFT JOIN FETCH r.supplier WHERE r.isDeleted = false")
    List<ReturnToSupplier> findAllWithAssociations();

    Optional<ReturnToSupplier> findByIdAndIsDeletedFalse(Long id);
}
