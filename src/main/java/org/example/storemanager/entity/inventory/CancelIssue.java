package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.enums.inventory.CancelIssueStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancel_issues", indexes = {
        @Index(name = "idx_cancel_issue_branch", columnList = "branch_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CancelIssue extends BaseEntity {

    @Column(name = "cancel_code", nullable = false, unique = true, length = 50)
    private String cancelCode;

    @Column(name = "cancel_date", nullable = false)
    private LocalDateTime cancelDate;

    @Column(name = "total_value", precision = 18, scale = 2)
    private BigDecimal totalValue;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CancelIssueStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}