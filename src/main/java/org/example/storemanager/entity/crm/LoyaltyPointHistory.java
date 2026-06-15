package org.example.storemanager.entity.crm;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.partnerarea.Customer;

@Entity
@Table(name = "loyalty_point_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class LoyaltyPointHistory extends BaseEntity {

    @Column(name = "points_change", nullable = false)
    private Integer pointsChange; // Số điểm thay đổi (+ hoặc -)

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType; // EARN (Tích lũy), REDEEM (Tiêu dùng), REFUND (Hoàn trả)

    @Column(name = "ref_code", length = 50)
    private String refCode; // Mã đơn hàng hoặc mã giao dịch phát sinh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}