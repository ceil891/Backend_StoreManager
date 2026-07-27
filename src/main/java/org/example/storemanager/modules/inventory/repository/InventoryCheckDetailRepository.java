package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.InventoryCheckDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryCheckDetailRepository extends JpaRepository<InventoryCheckDetail, Long> {
    List<InventoryCheckDetail> findByCheckIdAndIsDeletedFalse(Long checkId);
}
