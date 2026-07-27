package org.example.storemanager.modules.advancedaccounting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity
@Table(name = "chart_of_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ChartOfAccount extends BaseEntity {

    @Column(name = "account_code", nullable = false, unique = true, length = 50)
    private String accountCode;

    @Column(name = "account_name", nullable = false, length = 150)
    private String accountName;

    @Column(length = 50)
    private String type; // ASSET (Tài sản), LIABILITY (Nợ), EQUITY (Vốn), REVENUE (Doanh thu), EXPENSE (Chi phí)

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    // Optional: Tự tham chiếu để tạo cấu trúc cây (Tài khoản cha - Tài khoản con)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ChartOfAccount parent;
}