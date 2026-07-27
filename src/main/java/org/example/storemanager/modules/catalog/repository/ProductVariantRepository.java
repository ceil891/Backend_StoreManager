package org.example.storemanager.modules.catalog.repository;

import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findByIdAndIsDeletedFalse(Long id);
    List<ProductVariant> findByIsDeletedFalse();
    Optional<ProductVariant> findBySkuAndIsDeletedFalse(String sku);
    Optional<ProductVariant> findByBarcodeAndIsDeletedFalse(String barcode);
    Optional<ProductVariant> findByVariantCodeAndIsDeletedFalse(String variantCode);
    List<ProductVariant> findByProductIdAndIsDeletedFalse(Long productId);
    boolean existsBySkuAndIsDeletedFalse(String sku);
    boolean existsBySkuAndIdNotAndIsDeletedFalse(String sku, Long id);
    boolean existsByBarcodeAndIsDeletedFalse(String barcode);
    boolean existsByBarcodeAndIdNotAndIsDeletedFalse(String barcode, Long id);
    boolean existsByVariantCodeAndIsDeletedFalse(String variantCode);
}
