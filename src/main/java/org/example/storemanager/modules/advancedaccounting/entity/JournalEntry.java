package org.example.storemanager.modules.advancedaccounting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class JournalEntry extends BaseEntity {

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "reference_code", nullable = false, unique = true, length = 50)
    private String referenceCode; // Mã chứng từ gốc tham chiếu (VD mã Phiếu Thu/Chi, Hóa đơn)

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalAmount; // Tổng tiền của bút toán (Tổng Nợ = Tổng Có)
}