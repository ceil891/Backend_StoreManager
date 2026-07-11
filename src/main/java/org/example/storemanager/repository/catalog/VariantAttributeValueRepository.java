package org.example.storemanager.repository.catalog;

import org.example.storemanager.entity.catalog.VariantAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantAttributeValueRepository extends JpaRepository<VariantAttributeValue, Long> {
    List<VariantAttributeValue> findByProductVariantIdAndIsDeletedFalse(Long productVariantId);
}
