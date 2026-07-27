package org.example.storemanager.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch; // Import từ phân hệ hệ thống
import java.time.LocalDateTime;

@Entity
@Table(name = "price_lists", indexes = {
    @Index(name = "idx_pricelists_list_code", columnList = "list_code", unique = true),
    @Index(name = "idx_pricelists_branch_id", columnList = "branch_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PriceList extends BaseEntity {

    @Column(name = "list_code", nullable = false, unique = true, length = 50)
    private String listCode;

    @Column(name = "list_name", nullable = false, length = 150)
    private String listName;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch; // Áp dụng cho chi nhánh cụ thể (null nếu áp dụng toàn chuỗi)
}