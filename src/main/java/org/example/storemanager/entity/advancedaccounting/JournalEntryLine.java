package org.example.storemanager.entity.advancedaccounting;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "journal_entry_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class JournalEntryLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private ChartOfAccount account;

    @Column(name = "debit_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal debitAmount; // Số tiền Nợ

    @Column(name = "credit_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal creditAmount; // Số tiền Có

    @Column(columnDefinition = "TEXT")
    private String description; // Diễn giải chi tiết cho dòng này
}