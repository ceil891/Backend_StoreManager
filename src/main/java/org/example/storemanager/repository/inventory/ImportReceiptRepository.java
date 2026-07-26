package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.ImportReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, Long> {
    @Query("SELECT r FROM ImportReceipt r LEFT JOIN FETCH r.branch LEFT JOIN FETCH r.supplier LEFT JOIN FETCH r.purchaseOrder WHERE r.isDeleted = false")
    List<ImportReceipt> findAllWithAssociations();
    
    Optional<ImportReceipt> findByIdAndIsDeletedFalse(Long id);
    List<ImportReceipt> findByPurchaseOrderIdAndIsDeletedFalse(Long poId);
}
