package org.example.storemanager.modules.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.storemanager.modules.catalog.entity.Department;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Promotion extends BaseEntity {

    @Column(name = "promo_code", nullable = false, unique = true, length = 50)
    private String promoCode;

    @Column(name = "promo_name", nullable = false, length = 150)
    private String promoName;

    @Column(length = 50)
    private String type; // VD: PERCENTAGE (Phần trăm), FIXED_AMOUNT (Trừ tiền trực tiếp)

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal value; // Giá trị giảm

    @Column(name = "min_order_amount", precision = 18, scale = 2)
    private BigDecimal minOrderAmount; // Giá trị đơn hàng tối thiểu để áp dụng

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả CTKM

    @Column(length = 30)
    private String status; // ACTIVE, EXPIRED, DISABLED

    @Column(name = "usage_limit")
    private Integer usageLimit; // Giới hạn lượt dùng

    @Builder.Default
    @Column(name = "used_count", columnDefinition = "integer default 0")
    private Integer usedCount = 0; // Đã dùng bao nhiêu

    @Column(name = "max_discount_amount", precision = 18, scale = 2)
    private BigDecimal maxDiscountAmount; // Giảm tối đa

    @Column(name = "customer_type", length = 50)
    private String customerType; // Áp dụng nhóm khách

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id")
    private LoyaltyTier loyaltyTier; // Áp dụng hạng thành viên

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department; // Áp dụng cửa hàng

    @Builder.Default
    @Column(name = "is_stackable", columnDefinition = "boolean default false")
    private Boolean isStackable = false; // Có cộng dồn KM không

    @Builder.Default
    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true; // Bật/Tắt
}