package org.example.storemanager.modules.marketing.repository;

import org.example.storemanager.modules.marketing.entity.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
    Optional<CustomerFeedback> findByIdAndIsDeletedFalse(Long id);
    List<CustomerFeedback> findByIsDeletedFalse();
}
