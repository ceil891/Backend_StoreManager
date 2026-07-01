package org.example.storemanager.repository.hrm;

import org.example.storemanager.entity.hrm.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentHrmRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByIdAndIsDeletedFalse(Long id);

    boolean existsByDeptCodeAndIsDeletedFalse(String deptCode);

    boolean existsByDeptCodeAndIdNotAndIsDeletedFalse(String deptCode, Long id);

    @Query("SELECT d FROM Department d WHERE " +
           "(:includeDeleted = true OR d.isDeleted = false) AND " +
           "(:isActive IS NULL OR (:isActive = true AND (d.isLocked IS NULL OR d.isLocked = false)) OR (:isActive = false AND d.isLocked = true)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(d.deptCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.deptName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Department> findAllFiltered(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
