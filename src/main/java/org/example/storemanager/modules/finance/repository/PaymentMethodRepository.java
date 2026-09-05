package org.example.storemanager.modules.finance.repository;

import org.example.storemanager.modules.finance.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    Optional<PaymentMethod> findByIdAndIsDeletedFalse(Long id);

    List<PaymentMethod> findByIsDeletedFalse();

    List<PaymentMethod> findByIsDeletedFalseAndStatusOrderBySortOrderAsc(String status);

    /**
     * Lọc các PTTT khả dụng theo chi nhánh:
     * - Áp dụng toàn bộ chi nhánh (applyToAllBranches = true hoặc NULL), HOẶC
     * - Có bản ghi mapping trong payment_method_branches cho branchId này
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.isDeleted = false AND pm.status = 'ACTIVE' " +
           "AND (COALESCE(pm.applyToAllBranches, true) = true OR " +
           "     EXISTS (SELECT 1 FROM PaymentMethodBranch pmb WHERE pmb.paymentMethodId = pm.id AND pmb.branchId = :branchId AND (pmb.isDeleted IS NULL OR pmb.isDeleted = false))) " +
           "ORDER BY pm.sortOrder ASC")
    List<PaymentMethod> findActiveByBranchId(@Param("branchId") Long branchId);
}
