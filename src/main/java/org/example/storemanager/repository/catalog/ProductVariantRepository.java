package org.example.storemanager.repository.catalog;

import org.example.storemanager.entity.catalog.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findByIdAndIsDeletedFalse(Long id);
    Optional<ProductVariant> findBySkuAndIsDeletedFalse(String sku);
    Optional<ProductVariant> findByVariantCodeAndIsDeletedFalse(String variantCode);
    List<ProductVariant> findByProductIdAndIsDeletedFalse(Long productId);
    boolean existsBySkuAndIsDeletedFalse(String sku);
    boolean existsBySkuAndIdNotAndIsDeletedFalse(String sku, Long id);
    boolean existsByBarcodeAndIsDeletedFalse(String barcode);
    boolean existsByBarcodeAndIdNotAndIsDeletedFalse(String barcode, Long id);
    boolean existsByVariantCodeAndIsDeletedFalse(String variantCode);
}
