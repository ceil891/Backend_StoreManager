package org.example.storemanager.repository.sales;

import org.example.storemanager.entity.sales.ExportInvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExportInvoiceDetailRepository extends JpaRepository<ExportInvoiceDetail, Long> {
    Optional<ExportInvoiceDetail> findByIdAndIsDeletedFalse(Long id);
    List<ExportInvoiceDetail> findByInvoiceIdAndIsDeletedFalse(Long invoiceId);
}
