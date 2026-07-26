package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.ReturnToSupplierDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReturnToSupplierDetailRepository extends JpaRepository<ReturnToSupplierDetail, Long> {
    List<ReturnToSupplierDetail> findByReturnReceiptIdAndIsDeletedFalse(Long returnReceiptId);
}
