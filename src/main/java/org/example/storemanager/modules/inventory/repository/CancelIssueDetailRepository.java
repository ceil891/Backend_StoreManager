package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.CancelIssueDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CancelIssueDetailRepository extends JpaRepository<CancelIssueDetail, Long> {
    List<CancelIssueDetail> findByCancelIssueIdAndIsDeletedFalse(Long cancelIssueId);
}
