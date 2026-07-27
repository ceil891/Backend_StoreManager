package org.example.storemanager.modules.catalog.repository;

import org.example.storemanager.modules.catalog.entity.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    Optional<PriceList> findByIdAndIsDeletedFalse(Long id);

    boolean existsByListCodeAndIsDeletedFalse(String listCode);

    boolean existsByListCodeAndIdNotAndIsDeletedFalse(String listCode, Long id);

    List<PriceList> findByIsDeletedFalseOrderByCreatedAtDesc();

    @Query("""
            SELECT CASE WHEN COUNT(pl) > 0 THEN true ELSE false END
            FROM PriceList pl
            WHERE pl.isDeleted = false
              AND pl.isActive = true
              AND (:excludeId IS NULL OR pl.id <> :excludeId)
              AND pl.startDate IS NOT NULL
              AND pl.endDate IS NOT NULL
              AND :startDate < pl.endDate
              AND :endDate > pl.startDate
              AND (
                    (:branchId IS NULL AND pl.branch IS NULL)
                 OR (:branchId IS NOT NULL AND pl.branch.id = :branchId)
              )
            """)
    boolean existsOverlappingActive(
            @Param("branchId") Long branchId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("excludeId") Long excludeId);

    @Query("""
            SELECT pl FROM PriceList pl
            WHERE pl.isDeleted = false
              AND pl.isActive = true
              AND (pl.startDate IS NULL OR pl.startDate <= :at)
              AND (pl.endDate IS NULL OR pl.endDate >= :at)
              AND (
                    (:branchId IS NULL AND pl.branch IS NULL)
                 OR (pl.branch IS NULL)
                 OR (pl.branch.id = :branchId)
              )
            ORDER BY CASE WHEN pl.branch IS NULL THEN 1 ELSE 0 END, pl.startDate DESC
            """)
    List<PriceList> findActiveForBranch(@Param("branchId") Long branchId, @Param("at") LocalDateTime at);
}
