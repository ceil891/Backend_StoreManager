package org.example.storemanager.repository.advancedaccounting;

import org.example.storemanager.entity.advancedaccounting.AssetDisposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AssetDisposalRepository extends JpaRepository<AssetDisposal, Long> {
    Optional<AssetDisposal> findByIdAndIsDeletedFalse(Long id);
    List<AssetDisposal> findByIsDeletedFalse();
}
