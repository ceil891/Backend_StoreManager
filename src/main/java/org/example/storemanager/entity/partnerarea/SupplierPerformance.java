package org.example.storemanager.entity.partnerarea;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_performances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SupplierPerformance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Builder.Default
    @Column(name = "average_score", precision = 5, scale = 2)
    private BigDecimal averageScore = BigDecimal.ZERO;

    @Column(length = 30)
    private String ranking; // EXCELLENT, GOOD, FAIR, POOR
}
