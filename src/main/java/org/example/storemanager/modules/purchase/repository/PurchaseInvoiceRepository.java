package org.example.storemanager.modules.purchase.repository;

import org.example.storemanager.modules.purchase.entity.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {
    List<PurchaseInvoice> findByIsDeletedFalseOrderByCreatedAtDesc();
    Optional<PurchaseInvoice> findByIdAndIsDeletedFalse(Long id);
    Optional<PurchaseInvoice> findByInvoiceCodeAndIsDeletedFalse(String invoiceCode);
    boolean existsByInvoiceCodeAndIsDeletedFalse(String invoiceCode);
}
