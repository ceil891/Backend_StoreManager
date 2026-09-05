package org.example.storemanager.modules.system.repository;

import org.example.storemanager.modules.system.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByIdAndIsDeletedFalse(Long id);

    List<Branch> findByIsDeletedFalse();

    Optional<Branch> findById(Long id);

    boolean existsByBranchCodeAndIsDeletedFalse(String branchCode);

    boolean existsByBranchCodeAndIdNotAndIsDeletedFalse(String branchCode, Long id);

    Page<Branch> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT b FROM Branch b WHERE b.isDeleted = false AND " +
           "(cast(:isActive as boolean) IS NULL OR b.isActive = :isActive) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(b.branchName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.branchCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.address) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.phone) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Branch> findAllBranches(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @org.springframework.cache.annotation.Cacheable(value = "branches", key = "{#search, #isActive}")
    @Query("SELECT b FROM Branch b WHERE b.isDeleted = false AND " +
           "(cast(:isActive as boolean) IS NULL OR b.isActive = :isActive) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(b.branchName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.branchCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.address) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.phone) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    List<Branch> findAllBranchesList(
            @Param("search") String search,
            @Param("isActive") Boolean isActive);

    @Query("SELECT b FROM Branch b WHERE " +
           "(:includeDeleted = true OR b.isDeleted = false) AND " +
           "(cast(:isActive as boolean) IS NULL OR b.isActive = :isActive) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(b.branchName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.branchCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.address) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.phone) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Branch> findAllBranchesIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);

    @Query("SELECT b FROM Branch b WHERE " +
           "(:includeDeleted = true OR b.isDeleted = false) AND " +
           "(cast(:isActive as boolean) IS NULL OR b.isActive = :isActive) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(b.branchName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.branchCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.address) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(b.phone) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    List<Branch> findAllBranchesListIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted);
}
