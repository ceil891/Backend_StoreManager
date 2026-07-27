package org.example.storemanager.modules.advancedaccounting.repository;

import org.example.storemanager.modules.advancedaccounting.entity.AssetDisposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AssetDisposalRepository extends JpaRepository<AssetDisposal, Long> {
    Optional<AssetDisposal> findByIdAndIsDeletedFalse(Long id);
    List<AssetDisposal> findByIsDeletedFalse();
}
