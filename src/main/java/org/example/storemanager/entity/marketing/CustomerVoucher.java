package org.example.storemanager.entity.marketing;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.partnerarea.Customer;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CustomerVoucher extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt; // Ngày khách lưu mã

    @Column(name = "used_at")
    private LocalDateTime usedAt; // Ngày khách sử dụng mã (null nếu chưa dùng)

    @Column(nullable = false, length = 30)
    private String status; // UNUSED, USED, EXPIRED
}