package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.InventoryTransaction;
import org.example.storemanager.enums.inventory.InventoryTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    Optional<InventoryTransaction> findByTransactionCode(String transactionCode);

    List<InventoryTransaction> findByProductVariantIdOrderByCreatedAtDesc(Long productVariantId);

    Page<InventoryTransaction> findByProductVariantId(Long productVariantId, Pageable pageable);

    List<InventoryTransaction> findByTransactionTypeAndCreatedAtBetween(
            InventoryTransactionType type, LocalDateTime from, LocalDateTime to);
}
