package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.ImportReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImportReceiptDetailRepository extends JpaRepository<ImportReceiptDetail, Long> {
    List<ImportReceiptDetail> findByReceiptIdAndIsDeletedFalse(Long receiptId);
}
