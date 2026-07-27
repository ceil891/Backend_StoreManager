package org.example.storemanager.modules.advancedaccounting.repository;

import org.example.storemanager.modules.advancedaccounting.entity.JournalAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface JournalAttachmentRepository extends JpaRepository<JournalAttachment, Long> {
    Optional<JournalAttachment> findByIdAndIsDeletedFalse(Long id);
    List<JournalAttachment> findByIsDeletedFalse();
}
