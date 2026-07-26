package org.example.storemanager.repository.sales;

import org.example.storemanager.entity.sales.ExportInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExportInvoiceRepository extends JpaRepository<ExportInvoice, Long> {
    Optional<ExportInvoice> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT i FROM ExportInvoice i WHERE " +
           "(:includeDeleted = true OR i.isDeleted = false) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:branchId IS NULL OR i.branch.id = :branchId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.customer.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.note) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ExportInvoice> findAllInvoices(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
