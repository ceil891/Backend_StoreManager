package org.example.storemanager.repository.marketing;

import org.example.storemanager.entity.marketing.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    Optional<SupportTicket> findByIdAndIsDeletedFalse(Long id);
    List<SupportTicket> findByIsDeletedFalse();
}
