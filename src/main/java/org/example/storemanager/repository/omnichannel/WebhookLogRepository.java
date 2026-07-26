package org.example.storemanager.repository.omnichannel;

import org.example.storemanager.entity.omnichannel.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
    Optional<WebhookLog> findByIdAndIsDeletedFalse(Long id);
    List<WebhookLog> findByIsDeletedFalse();
}
