package org.example.storemanager.modules.marketing.repository;

import org.example.storemanager.modules.marketing.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    Optional<SupportTicket> findByIdAndIsDeletedFalse(Long id);
    List<SupportTicket> findByIsDeletedFalse();
}
