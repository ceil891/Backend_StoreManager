package org.example.storemanager.repository.catalog;

import org.example.storemanager.entity.catalog.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    Optional<AttributeValue> findByIdAndIsDeletedFalse(Long id);
    List<AttributeValue> findByProductAttributeIdAndIsDeletedFalse(Long attributeId);
}
