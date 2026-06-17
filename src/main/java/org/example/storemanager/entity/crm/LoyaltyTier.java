package org.example.storemanager.entity.crm;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "loyalty_tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class LoyaltyTier extends BaseEntity {

    @Column(name = "tier_name", nullable = false, unique = true, length = 50)
    private String tierName;

    @Column(name = "min_points", nullable = false)
    private Integer minPoints; // Số điểm tối thiểu để đạt hạng này

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent; // % Giảm giá mặc định cho hạng này
}