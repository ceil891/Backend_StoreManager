package org.example.storemanager.repository.marketing;

import org.example.storemanager.entity.marketing.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
    Optional<CustomerFeedback> findByIdAndIsDeletedFalse(Long id);
    List<CustomerFeedback> findByIsDeletedFalse();
}
