package org.example.storemanager.modules.logistics.repository;

import org.example.storemanager.modules.logistics.entity.DeliveryAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryAssignmentHistoryRepository extends JpaRepository<DeliveryAssignmentHistory, Long> {
    List<DeliveryAssignmentHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    List<DeliveryAssignmentHistory> findByOrderIdAndIsDeletedFalseOrderByCreatedAtDesc(Long orderId);
}
