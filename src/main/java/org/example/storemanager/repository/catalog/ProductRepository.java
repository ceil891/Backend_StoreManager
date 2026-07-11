package org.example.storemanager.repository.catalog;

import org.example.storemanager.entity.catalog.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndIsDeletedFalse(Long id);

    boolean existsByProductCodeAndIsDeletedFalse(String productCode);

    boolean existsByProductCodeAndIdNotAndIsDeletedFalse(String productCode, Long id);

    boolean existsByBarcodeAndIsDeletedFalse(String barcode);

    boolean existsByBarcodeAndIdNotAndIsDeletedFalse(String barcode, Long id);

    @Query("SELECT p FROM Product p WHERE " +
           "(:includeDeleted = true OR p.isDeleted = false) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.productCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.barcode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findAllProductsIncludeDeleted(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
