package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.CancelIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CancelIssueRepository extends JpaRepository<CancelIssue, Long> {
    @Query("SELECT c FROM CancelIssue c LEFT JOIN FETCH c.branch WHERE c.isDeleted = false")
    List<CancelIssue> findAllWithAssociations();

    Optional<CancelIssue> findByIdAndIsDeletedFalse(Long id);
}
