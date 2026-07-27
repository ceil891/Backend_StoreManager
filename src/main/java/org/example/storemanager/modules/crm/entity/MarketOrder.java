package org.example.storemanager.modules.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.partnerarea.entity.Customer;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.example.storemanager.modules.system.entity.User;

@Entity
@Table(name = "market_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class MarketOrder extends BaseEntity {

    @Column(name = "gps_location", length = 255)
    private String gpsLocation; // Tọa độ check-in khi lên đơn

    @Column(name = "expected_date")
    private LocalDateTime expectedDate;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "order_code", unique = true, length = 50)
    private String orderCode; // Mã đơn

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount; // Tổng tiền

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount; // Giảm giá

    @Column(name = "check_in_image", length = 500)
    private String checkInImage; // Ảnh check-in

    private Double latitude; // Vĩ độ
    private Double longitude; // Kinh độ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private User employee; // Nhân viên thị trường (User)

    @Column(name = "visit_id")
    private Long visitId; // Chuyến viếng thăm

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion; // CTKM áp dụng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}