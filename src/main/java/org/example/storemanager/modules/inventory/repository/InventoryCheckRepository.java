package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.InventoryCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryCheckRepository extends JpaRepository<InventoryCheck, Long> {
    boolean existsByCheckCode(String checkCode);
}
