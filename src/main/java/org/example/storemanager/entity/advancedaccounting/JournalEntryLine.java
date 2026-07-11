package org.example.storemanager.entity.advancedaccounting;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;
import org.example.storemanager.entity.catalog.Department;
import org.example.storemanager.entity.partnerarea.Customer;
import org.example.storemanager.entity.partnerarea.Supplier;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter; // Trung tâm chi phí

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department; // Phòng ban

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer; // Khách hàng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier; // Nhà cung cấp

    @Column(precision = 18, scale = 3)
    private BigDecimal quantity; // Số lượng

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice; // Đơn giá
}