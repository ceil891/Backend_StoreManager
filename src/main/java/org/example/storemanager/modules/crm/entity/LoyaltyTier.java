package org.example.storemanager.modules.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "loyalty_tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class LoyaltyTier extends BaseEntity {

    @Column(name = "tier_code", nullable = false, unique = true, length = 30)
    private String tierCode;

    @Column(name = "tier_name", nullable = false, unique = true, length = 50)
    private String tierName;

    @Column(name = "min_points", nullable = false)
    private Integer minPoints; // Số điểm tối thiểu để đạt hạng này

    @Column(name = "max_points")
    private Integer maxPoints; // Giới hạn điểm của hạng (nếu áp dụng)

    @Column(name = "min_spend", precision = 15, scale = 2)
    private BigDecimal minSpend; // Mốc tổng chi tiêu tối thiểu (VNĐ)

    @Column(name = "max_spend", precision = 15, scale = 2)
    private BigDecimal maxSpend; // Mốc tổng chi tiêu tối đa (VNĐ)

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent; // % Giảm giá mặc định cho hạng này

    @Column(name = "point_multiplier", precision = 5, scale = 2)
    private BigDecimal pointMultiplier; // Hệ số nhân điểm (VD: Gold x1.5)

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả hạng thành viên

    @Column(columnDefinition = "TEXT")
    private String benefits; // Quyền lợi của hạng

    @Builder.Default
    @Column(name = "is_default", columnDefinition = "boolean default false")
    private Boolean isDefault = false; // Hạng mặc định cho khách mới

    @Builder.Default
    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true; // Bật/Tắt hạng

    @Column(name = "display_order")
    private Integer displayOrder; // Thứ tự hiển thị
}