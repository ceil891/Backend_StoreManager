package org.example.storemanager.modules.warranty.repository;

import org.example.storemanager.modules.warranty.entity.SupplierEvaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierEvaluationRepository extends JpaRepository<SupplierEvaluation, Long> {
    Optional<SupplierEvaluation> findByIdAndIsDeletedFalse(Long id);

    List<SupplierEvaluation> findBySupplierIdAndIsDeletedFalse(Long supplierId);

    @Query("SELECT se FROM SupplierEvaluation se WHERE " +
           "(:includeDeleted = true OR se.isDeleted = false) AND " +
           "(cast(:supplierId as long) IS NULL OR se.supplier.id = cast(:supplierId as long)) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(se.remarks) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(se.result) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(se.note) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<SupplierEvaluation> findAllEvaluations(
            @Param("search") String search,
            @Param("supplierId") Long supplierId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
