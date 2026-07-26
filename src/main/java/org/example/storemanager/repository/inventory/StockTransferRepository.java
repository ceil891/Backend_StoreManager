package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    @Query("SELECT t FROM StockTransfer t LEFT JOIN FETCH t.fromBranch LEFT JOIN FETCH t.toBranch WHERE t.isDeleted = false")
    List<StockTransfer> findAllWithAssociations();

    Optional<StockTransfer> findByIdAndIsDeletedFalse(Long id);
}
