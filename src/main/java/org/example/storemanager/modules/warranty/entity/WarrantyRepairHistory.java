package org.example.storemanager.modules.warranty.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "warranty_repair_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WarrantyRepairHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warranty_claim_id", nullable = false)
    private WarrantyClaim warrantyClaim;

    @Column(name = "repair_date", nullable = false)
    private LocalDateTime repairDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician; // Kỹ thuật viên (User)

    @Column(nullable = false, length = 255)
    private String action; // Action taken

    @Column(columnDefinition = "TEXT")
    private String description; // Description of repair

    @Column(name = "replaced_part", length = 255)
    private String replacedPart; // Replaced part

    @Builder.Default
    @Column(precision = 18, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO; // Cost of repair
}
