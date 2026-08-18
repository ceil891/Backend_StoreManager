package org.example.storemanager.modules.marketing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.partnerarea.entity.Customer;

import java.time.LocalDateTime;
import org.example.storemanager.modules.sales.entity.SaleOrder;
import org.example.storemanager.modules.system.entity.User;

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

    @Column(name = "voucher_code", length = 50)
    private String voucherCode; // Mã voucher cụ thể cấp cho khách hàng

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt; // Ngày khách lưu mã / ngày phát hành

    @Column(name = "used_at")
    private LocalDateTime usedAt; // Ngày khách sử dụng mã (null nếu chưa dùng)

    @Column(nullable = false, length = 30)
    private String status; // ACTIVE, USED, EXPIRED, CANCELLED

    @Column(name = "expired_at")
    private LocalDateTime expiredAt; // Ngày hết hạn voucher

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_order_id")
    private SaleOrder usedOrder; // Đơn hàng đã dùng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy; // Người cấp voucher
}