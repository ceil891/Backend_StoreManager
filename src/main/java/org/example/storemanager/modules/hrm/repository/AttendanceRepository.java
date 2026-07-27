package org.example.storemanager.modules.hrm.repository;

import org.example.storemanager.modules.hrm.entity.Attendance;
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
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByIdAndIsDeletedFalse(Long id);

    Optional<Attendance> findByUserIdAndWorkDateAndIsDeletedFalse(Long userId, LocalDate workDate);

    @Query("SELECT a FROM Attendance a WHERE " +
           "(:includeDeleted = true OR a.isDeleted = false) AND " +
           "(:isActive IS NULL OR (:isActive = true AND (a.isLocked IS NULL OR a.isLocked = false)) OR (:isActive = false AND a.isLocked = true)) AND " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:status IS NULL OR :status = '' OR a.status = :status) AND " +
           "(:workDateFrom IS NULL OR a.workDate >= :workDateFrom) AND " +
           "(:workDateTo IS NULL OR a.workDate <= :workDateTo) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(a.status) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.gpsLocation) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Attendance> findAllFiltered(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("workDateFrom") LocalDate workDateFrom,
            @Param("workDateTo") LocalDate workDateTo,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.isDeleted = false AND a.user.id = :userId " +
           "AND a.workDate >= :fromDate AND a.workDate <= :toDate")
    List<Attendance> findByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("SELECT a FROM Attendance a WHERE a.isDeleted = false " +
           "AND a.workDate >= :fromDate AND a.workDate <= :toDate " +
           "AND a.user.id IN (" +
           "  SELECT c.user.id FROM EmployeeContract c WHERE c.isDeleted = false " +
           "  AND c.position.department.id = :departmentId)")
    List<Attendance> findByDepartmentAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
