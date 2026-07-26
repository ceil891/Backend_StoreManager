package org.example.storemanager.repository.catalog;

import org.example.storemanager.entity.catalog.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.cache.annotation.Cacheable;
import java.util.List;
import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {

    @Cacheable(value = "sizes", key = "#id")
    Optional<Size> findByIdAndIsDeletedFalse(Long id);

    @Cacheable(value = "sizes", key = "#id")
    Optional<Size> findById(Long id);

    boolean existsBySizeCodeAndIsDeletedFalse(String sizeCode);

    boolean existsBySizeCodeAndIdNotAndIsDeletedFalse(String sizeCode, Long id);

    @Query("SELECT s FROM CatalogSize s WHERE s.isDeleted = false AND " +
           "(:isActive IS NULL OR s.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(s.sizeName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.sizeCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Size> findAllSizes(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT s FROM CatalogSize s WHERE s.isDeleted = false AND " +
           "(:isActive IS NULL OR s.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(s.sizeName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.sizeCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Size> findAllSizesList(
            @Param("search") String search,
            @Param("isActive") Boolean isActive);

    @Query("SELECT s FROM CatalogSize s WHERE " +
           "(:includeDeleted = true OR s.isDeleted = false) AND " +
           "(:isActive IS NULL OR s.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(s.sizeName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.sizeCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Size> findAllSizesIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);

    @Query("SELECT s FROM CatalogSize s WHERE " +
           "(:includeDeleted = true OR s.isDeleted = false) AND " +
           "(:isActive IS NULL OR s.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(s.sizeName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.sizeCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Size> findAllSizesListIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted);
}
