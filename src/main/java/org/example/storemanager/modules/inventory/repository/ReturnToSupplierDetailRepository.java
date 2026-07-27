package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.ReturnToSupplierDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReturnToSupplierDetailRepository extends JpaRepository<ReturnToSupplierDetail, Long> {
    List<ReturnToSupplierDetail> findByReturnReceiptIdAndIsDeletedFalse(Long returnReceiptId);
}
