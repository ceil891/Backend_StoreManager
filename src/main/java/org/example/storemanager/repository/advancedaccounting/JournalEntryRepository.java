package org.example.storemanager.repository.advancedaccounting;

import org.example.storemanager.entity.advancedaccounting.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    Optional<JournalEntry> findByIdAndIsDeletedFalse(Long id);
    List<JournalEntry> findByIsDeletedFalse();
    Optional<JournalEntry> findByReferenceCodeAndIsDeletedFalse(String referenceCode);
}
