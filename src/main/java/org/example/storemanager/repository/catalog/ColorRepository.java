package org.example.storemanager.repository.catalog;

import org.example.storemanager.entity.catalog.Color;
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
public interface ColorRepository extends JpaRepository<Color, Long> {

    @Cacheable(value = "colors", key = "#id")
    Optional<Color> findByIdAndIsDeletedFalse(Long id);

    @Cacheable(value = "colors", key = "#id")
    Optional<Color> findById(Long id);

    boolean existsByColorCodeAndIsDeletedFalse(String colorCode);

    boolean existsByColorCodeAndIdNotAndIsDeletedFalse(String colorCode, Long id);

    @Query("SELECT c FROM CatalogColor c WHERE c.isDeleted = false AND " +
           "(:isActive IS NULL OR c.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.colorName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.colorCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Color> findAllColors(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT c FROM CatalogColor c WHERE c.isDeleted = false AND " +
           "(:isActive IS NULL OR c.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.colorName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.colorCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Color> findAllColorsList(
            @Param("search") String search,
            @Param("isActive") Boolean isActive);

    @Query("SELECT c FROM CatalogColor c WHERE " +
           "(:includeDeleted = true OR c.isDeleted = false) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.colorName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.colorCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Color> findAllColorsIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);

    @Query("SELECT c FROM CatalogColor c WHERE " +
           "(:includeDeleted = true OR c.isDeleted = false) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.colorName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.colorCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Color> findAllColorsListIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted);
}
