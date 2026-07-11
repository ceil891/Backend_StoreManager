package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.InventoryCheckDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryCheckDetailRepository extends JpaRepository<InventoryCheckDetail, Long> {
}
