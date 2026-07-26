package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.ImportReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImportReceiptDetailRepository extends JpaRepository<ImportReceiptDetail, Long> {
    List<ImportReceiptDetail> findByReceiptIdAndIsDeletedFalse(Long receiptId);
}
