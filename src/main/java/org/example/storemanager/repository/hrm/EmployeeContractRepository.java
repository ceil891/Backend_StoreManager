package org.example.storemanager.repository.hrm;

import org.example.storemanager.entity.hrm.EmployeeContract;
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
public interface EmployeeContractRepository extends JpaRepository<EmployeeContract, Long> {

    Optional<EmployeeContract> findByIdAndIsDeletedFalse(Long id);

    boolean existsByContractNumberAndIsDeletedFalse(String contractNumber);

    boolean existsByContractNumberAndIdNotAndIsDeletedFalse(String contractNumber, Long id);

    List<EmployeeContract> findByUserIdAndIsDeletedFalse(Long userId);

    Optional<EmployeeContract> findByUserIdAndStatusAndIsDeletedFalse(Long userId, String status);

    @Query("SELECT c FROM EmployeeContract c WHERE c.user.id = :userId AND c.isDeleted = false ORDER BY c.startDate DESC")
    List<EmployeeContract> findContractHistoryByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM EmployeeContract c WHERE " +
           "(:includeDeleted = true OR c.isDeleted = false) AND " +
           "(:isActive IS NULL OR (:isActive = true AND (c.isLocked IS NULL OR c.isLocked = false)) OR (:isActive = false AND c.isLocked = true)) AND " +
           "(:userId IS NULL OR c.user.id = :userId) AND " +
           "(:status IS NULL OR :status = '' OR c.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.contractNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.contractType) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.status) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<EmployeeContract> findAllFiltered(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);

    @Query("SELECT c FROM EmployeeContract c WHERE " +
           "c.isDeleted = false AND " +
           "c.endDate IS NOT NULL AND " +
           "c.endDate > CURRENT_DATE AND " +
           "c.endDate <= :thresholdDate " +
           "ORDER BY c.endDate ASC")
    List<EmployeeContract> findExpiringContracts(@Param("thresholdDate") java.time.LocalDate thresholdDate);
}
