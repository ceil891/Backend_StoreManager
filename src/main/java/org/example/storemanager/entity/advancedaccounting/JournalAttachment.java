package org.example.storemanager.entity.advancedaccounting;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "journal_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class JournalAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;
}
