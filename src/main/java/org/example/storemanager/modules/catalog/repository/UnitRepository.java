package org.example.storemanager.modules.catalog.repository;

import org.example.storemanager.modules.catalog.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    Optional<Unit> findByIdAndIsDeletedFalse(Long id);

    // Tìm bất kỳ (kể cả đã xóa) — dùng cho admin xem lịch sử
    Optional<Unit> findById(Long id);

    boolean existsByUnitCodeAndIsDeletedFalse(String unitCode);

    boolean existsByUnitCodeAndIdNotAndIsDeletedFalse(String unitCode, Long id);

    // ==== Query CHỈ lấy chưa xóa (isDeleted = false) ====
    @Query("SELECT u FROM Unit u WHERE u.isDeleted = false AND " +
           "(cast(:isActive as boolean) IS NULL OR u.isActive = :isActive) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(u.unitName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(u.unitCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(u.description) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Unit> findAllUnits(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT u FROM Unit u WHERE u.isDeleted = false AND " +
           "(cast(:isActive as boolean) IS NULL OR u.isActive = :isActive) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(u.unitName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(u.unitCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(u.description) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    List<Unit> findAllUnitsList(
            @Param("search") String search,
            @Param("isActive") Boolean isActive);

    // ==== Query lấy TẤT CẢ kể cả đã xóa (includeDeleted = true) ====
    @Query("SELECT u FROM Unit u WHERE " +
           "(:includeDeleted = true OR u.isDeleted = false) AND " +
           "(cast(:isActive as boolean) IS NULL OR u.isActive = :isActive) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(u.unitName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(u.unitCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(u.description) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Unit> findAllUnitsIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);

    @Query("SELECT u FROM Unit u WHERE " +
           "(:includeDeleted = true OR u.isDeleted = false) AND " +
           "(cast(:isActive as boolean) IS NULL OR u.isActive = :isActive) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(u.unitName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(u.unitCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(u.description) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    List<Unit> findAllUnitsListIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted);
}
