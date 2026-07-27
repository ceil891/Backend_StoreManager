package org.example.storemanager.modules.finance.repository;

import org.example.storemanager.modules.finance.entity.OperatingCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface OperatingCostRepository extends JpaRepository<OperatingCost, Long> {
    Optional<OperatingCost> findByIdAndIsDeletedFalse(Long id);
    List<OperatingCost> findByIsDeletedFalse();
}
