package org.example.storemanager.repository.warranty;

import org.example.storemanager.entity.warranty.SupplierContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierContractRepository extends JpaRepository<SupplierContract, Long> {
    Optional<SupplierContract> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT sc FROM SupplierContract sc WHERE sc.isDeleted = false AND sc.status = 'ACTIVE'")
    List<SupplierContract> findActiveContracts();

    @Query("SELECT sc FROM SupplierContract sc WHERE sc.isDeleted = false AND sc.status = 'ACTIVE' AND sc.endDate <= :date")
    List<SupplierContract> findExpiringContracts(@Param("date") LocalDate date);

    @Query("SELECT sc FROM SupplierContract sc WHERE " +
           "(:includeDeleted = true OR sc.isDeleted = false) AND " +
           "(:status IS NULL OR sc.status = :status) AND " +
           "(:supplierId IS NULL OR sc.supplier.id = :supplierId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(sc.contractCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.contractName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.signedBy) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.note) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SupplierContract> findAllContracts(
            @Param("search") String search,
            @Param("status") String status,
            @Param("supplierId") Long supplierId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
