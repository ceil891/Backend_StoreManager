package org.example.storemanager.modules.marketing.repository;

import org.example.storemanager.modules.marketing.entity.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {
    Optional<TicketMessage> findByIdAndIsDeletedFalse(Long id);
    List<TicketMessage> findByIsDeletedFalse();
}
