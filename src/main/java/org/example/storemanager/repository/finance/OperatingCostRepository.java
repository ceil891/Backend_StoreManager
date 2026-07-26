package org.example.storemanager.repository.finance;

import org.example.storemanager.entity.finance.OperatingCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface OperatingCostRepository extends JpaRepository<OperatingCost, Long> {
    Optional<OperatingCost> findByIdAndIsDeletedFalse(Long id);
    List<OperatingCost> findByIsDeletedFalse();
}
