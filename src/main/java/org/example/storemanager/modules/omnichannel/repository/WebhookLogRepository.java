package org.example.storemanager.modules.omnichannel.repository;

import org.example.storemanager.modules.omnichannel.entity.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
    Optional<WebhookLog> findByIdAndIsDeletedFalse(Long id);
    List<WebhookLog> findByIsDeletedFalse();
}
