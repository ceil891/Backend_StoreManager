package org.example.storemanager.entity.sales;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.enums.sales.OrderStatus;
import org.example.storemanager.enums.sales.PaymentStatus;
import org.example.storemanager.enums.sales.OrderOrigin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale_orders", indexes = {
        @Index(name = "idx_sale_order_code", columnList = "order_code", unique = true),
        @Index(name = "idx_sale_order_customer", columnList = "customer_id"),
        @Index(name = "idx_sale_order_branch", columnList = "branch_id"),
        @Index(name = "idx_sale_order_status", columnList = "status"),
        @Index(name = "idx_sale_order_deleted", columnList = "is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrder extends BaseEntity {

    @Column(name = "order_code", nullable = false, length = 50)
    private String orderCode;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "user_id")
    private Long userId; // Nhân viên tạo đơn

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_origin", nullable = false, length = 30)
    private OrderOrigin orderOrigin;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "final_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "saleOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleOrderDetail> details = new ArrayList<>();

    public void addDetail(SaleOrderDetail detail) {
        details.add(detail);
        detail.setSaleOrder(this);
    }
}