package org.example.storemanager.modules.sales.repository;

import org.example.storemanager.modules.sales.entity.ExportInvoice;
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

    java.util.List<ExportInvoice> findByPosSessionId(Long posSessionId);

    @Query("SELECT i FROM ExportInvoice i WHERE " +
           "(:includeDeleted = true OR i.isDeleted = false) AND " +
           "(cast(:status as string) IS NULL OR cast(:status as string) = '' OR i.status = :status) AND " +
           "(cast(:branchId as long) IS NULL OR i.branch.id = :branchId) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(i.customer.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(i.note) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<ExportInvoice> findAllInvoices(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
