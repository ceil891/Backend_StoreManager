package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.InventoryCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryCheckRepository extends JpaRepository<InventoryCheck, Long> {
    boolean existsByCheckCode(String checkCode);
}
