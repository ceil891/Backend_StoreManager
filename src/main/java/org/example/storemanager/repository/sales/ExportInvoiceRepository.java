package org.example.storemanager.repository.sales;

import org.example.storemanager.entity.sales.ExportInvoice;
import org.example.storemanager.enums.sales.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExportInvoiceRepository extends JpaRepository<ExportInvoice, Long> {

    // Tìm kiếm hóa đơn chưa bị xóa (Soft delete)
    Page<ExportInvoice> findByIsDeletedFalse(Pageable pageable);

    // Lấy danh sách hóa đơn theo trạng thái (Ví dụ: PAID, DRAFT) và chưa bị xóa
    List<ExportInvoice> findByStatusAndIsDeletedFalse(OrderStatus status);

    Optional<ExportInvoice> findByIdAndIsDeletedFalse(Long id);

    boolean existsByInvoiceCode(String invoiceCode);

    // Tìm kiếm theo mã hóa đơn, chi nhánh...
    @Query("SELECT e FROM ExportInvoice e WHERE e.isDeleted = false " +
            "AND (:keyword IS NULL OR e.invoiceCode LIKE %:keyword%) " +
            "AND (:branchId IS NULL OR e.branch.id = :branchId)")
    Page<ExportInvoice> searchInvoices(@Param("keyword") String keyword,
                                       @Param("branchId") Long branchId,
                                       Pageable pageable);
}