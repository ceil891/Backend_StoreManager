package org.example.storemanager.modules.advancedaccounting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "depreciation_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DepreciationHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private FixedAsset asset;

    @Column(name = "depreciation_date", nullable = false)
    private LocalDate depreciationDate; // Ngày thực hiện trích khấu hao

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount; // Giá trị khấu hao kỳ này

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal accumulated; // Lũy kế khấu hao đến thời điểm hiện tại

    @Column(name = "net_value", precision = 18, scale = 2, nullable = false)
    private BigDecimal netValue; // Giá trị còn lại (Nguyên giá - Lũy kế)
}