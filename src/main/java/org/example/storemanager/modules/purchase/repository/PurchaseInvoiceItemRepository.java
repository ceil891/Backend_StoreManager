package org.example.storemanager.modules.purchase.repository;

import org.example.storemanager.modules.purchase.entity.PurchaseInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseInvoiceItemRepository extends JpaRepository<PurchaseInvoiceItem, Long> {
    List<PurchaseInvoiceItem> findByPurchaseInvoiceIdAndIsDeletedFalse(Long purchaseInvoiceId);
}
