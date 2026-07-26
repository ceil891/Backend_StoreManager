package org.example.storemanager.repository.advancedaccounting;

import org.example.storemanager.entity.advancedaccounting.FixedAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface FixedAssetRepository extends JpaRepository<FixedAsset, Long> {
    Optional<FixedAsset> findByIdAndIsDeletedFalse(Long id);
    List<FixedAsset> findByIsDeletedFalse();
}
