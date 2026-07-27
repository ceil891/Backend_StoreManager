package org.example.storemanager.modules.catalog.repository;

import org.example.storemanager.modules.catalog.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    Optional<ProductAttribute> findByIdAndIsDeletedFalse(Long id);
    Optional<ProductAttribute> findByAttributeCodeAndIsDeletedFalse(String attributeCode);
    boolean existsByAttributeCodeAndIsDeletedFalse(String attributeCode);
    boolean existsByAttributeCodeAndIdNotAndIsDeletedFalse(String attributeCode, Long id);
    List<ProductAttribute> findAllByIsDeletedFalse();
}
