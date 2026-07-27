package org.example.storemanager.modules.advancedaccounting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "asset_disposals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AssetDisposal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private FixedAsset asset;

    @Column(name = "dispose_date", nullable = false)
    private LocalDate disposeDate;

    @Column(name = "sale_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal saleAmount;

    @Column(columnDefinition = "TEXT")
    private String reason;
}
