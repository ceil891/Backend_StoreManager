package org.example.storemanager.modules.advancedaccounting.repository;

import org.example.storemanager.modules.advancedaccounting.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {
    Optional<JournalEntryLine> findByIdAndIsDeletedFalse(Long id);
    List<JournalEntryLine> findByIsDeletedFalse();
    List<JournalEntryLine> findByJournalEntryIdAndIsDeletedFalse(Long journalEntryId);
}
