package org.example.storemanager.repository.hrm;

import org.example.storemanager.entity.hrm.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByIdAndIsDeletedFalse(Long id);

    boolean existsByPositionCodeAndIsDeletedFalse(String positionCode);

    boolean existsByPositionCodeAndIdNotAndIsDeletedFalse(String positionCode, Long id);

    @Query(value = "SELECT MAX(CAST(position_code AS INTEGER)) FROM positions WHERE is_deleted = false", nativeQuery = true)
    Optional<Integer> findMaxPositionCodeAsInteger();

    @Query("SELECT p FROM Position p WHERE " +
            "(:includeDeleted = true OR p.isDeleted = false) AND " +
            "(:isActive IS NULL OR (:isActive = true AND (p.isLocked IS NULL OR p.isLocked = false)) OR (:isActive = false AND p.isLocked = true)) AND " +
            "(:departmentId IS NULL OR p.department.id = :departmentId) AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(p.positionCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.positionName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Position> findAllFiltered(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("departmentId") Long departmentId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);

    List<Position> findAllByIsDeletedFalseAndIsLockedFalseOrderByPositionNameAsc();

    List<Position> findByPositionCodeContainsIgnoreCaseOrPositionNameContainsIgnoreCaseAndIsDeletedFalse(String positionCode, String positionName);
}
