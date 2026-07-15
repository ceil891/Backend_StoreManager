package org.example.storemanager.repository.sales;

import org.example.storemanager.entity.sales.ExportInvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportInvoiceDetailRepository extends JpaRepository<ExportInvoiceDetail, Long> {
    List<ExportInvoiceDetail> findByInvoiceId(Long invoiceId);
    void deleteByInvoiceId(Long invoiceId);
}