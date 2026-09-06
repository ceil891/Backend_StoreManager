package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.TransferShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferShipmentRepository extends JpaRepository<TransferShipment, Long> {

    boolean existsByTrackingCode(String trackingCode);

    @Query("SELECT COUNT(ts) FROM TransferShipment ts WHERE ts.trackingCode LIKE :prefix%")
    long countByTrackingCodePrefix(@Param("prefix") String prefix);

    @Query("SELECT ts FROM TransferShipment ts LEFT JOIN FETCH ts.transfer t LEFT JOIN FETCH t.fromBranch LEFT JOIN FETCH t.toBranch WHERE ts.isDeleted = false ORDER BY ts.createdAt DESC")
    List<TransferShipment> findAllWithTransferDetails();

    Optional<TransferShipment> findByTransferIdAndIsDeletedFalse(Long transferId);

    Optional<TransferShipment> findByIdAndIsDeletedFalse(Long id);
}
