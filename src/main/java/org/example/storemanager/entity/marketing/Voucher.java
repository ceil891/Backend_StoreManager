package org.example.storemanager.entity.marketing;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;

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

    @Column(name = "current_usage", columnDefinition = "integer default 0")
    private Integer currentUsage = 0; // Số lượt đã sử dụng
}