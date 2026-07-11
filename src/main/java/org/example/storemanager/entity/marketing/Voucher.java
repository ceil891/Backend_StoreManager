package org.example.storemanager.entity.marketing;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Voucher extends BaseEntity {

    @Column(name = "voucher_code", nullable = false, unique = true, length = 50)
    private String voucherCode;

    @Column(nullable = false, length = 50)
    private String type; // PERCENTAGE, FIXED_AMOUNT, FREE_SHIP

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal value; // Mức giảm giá

    @Column(name = "max_usage")
    private Integer maxUsage; // Tổng số lượt sử dụng tối đa

    @Builder.Default
    @Column(name = "current_usage", columnDefinition = "integer default 0")
    private Integer currentUsage = 0; // Số lượt đã sử dụng

    @Column(name = "voucher_name", length = 150)
    private String voucherName; // Tên voucher

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả

    @Column(name = "min_order_amount", precision = 18, scale = 2)
    private BigDecimal minOrderAmount; // Đơn tối thiểu

    @Column(name = "max_discount_amount", precision = 18, scale = 2)
    private BigDecimal maxDiscountAmount; // Giảm tối đa

    @Column(name = "start_date")
    private LocalDateTime startDate; // Ngày bắt đầu

    @Column(name = "end_date")
    private LocalDateTime endDate; // Ngày kết thúc

    @Column(length = 30)
    private String status; // ACTIVE, EXPIRED, DISABLED

    @Builder.Default
    @Column(name = "is_public", columnDefinition = "boolean default true")
    private Boolean isPublic = true; // Voucher công khai

    @Builder.Default
    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true; // Bật/Tắt

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private MarketingCampaign campaign; // Thuộc chiến dịch nào
}