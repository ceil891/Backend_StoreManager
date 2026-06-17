package org.example.storemanager.entity.crm;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}