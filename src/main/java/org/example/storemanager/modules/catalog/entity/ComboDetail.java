package org.example.storemanager.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import java.math.BigDecimal;

@Entity
@Table(name = "combo_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ComboDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(precision = 12, scale = 3, nullable = false)
    private BigDecimal quantity;

    /** Snapshot giá lẻ tại thời điểm tạo combo — dùng cảnh báo giá combo. */
    @Column(name = "unit_price_at_creation", precision = 18, scale = 2)
    private BigDecimal unitPriceAtCreation;
}