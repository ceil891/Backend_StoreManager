package org.example.storemanager.repository.advancedaccounting;

import org.example.storemanager.entity.advancedaccounting.JournalEntryLine;
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
