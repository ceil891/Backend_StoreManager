package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.CancelIssueDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CancelIssueDetailRepository extends JpaRepository<CancelIssueDetail, Long> {
    List<CancelIssueDetail> findByCancelIssueIdAndIsDeletedFalse(Long cancelIssueId);
}
