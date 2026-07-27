package org.example.storemanager.modules.sales.repository;

import org.example.storemanager.modules.sales.entity.CustomerReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerReturnRepository extends JpaRepository<CustomerReturn, Long> {
    Optional<CustomerReturn> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT r FROM CustomerReturn r WHERE " +
           "(:includeDeleted = true OR r.isDeleted = false) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:branchId IS NULL OR r.branch.id = :branchId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(r.returnCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.customer.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.reason) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.note) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<CustomerReturn> findAllReturns(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
