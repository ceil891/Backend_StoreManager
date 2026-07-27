package org.example.storemanager.modules.catalog.repository;

import org.example.storemanager.modules.catalog.entity.VariantAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantAttributeValueRepository extends JpaRepository<VariantAttributeValue, Long> {
    List<VariantAttributeValue> findByProductVariantIdAndIsDeletedFalse(Long productVariantId);
}
