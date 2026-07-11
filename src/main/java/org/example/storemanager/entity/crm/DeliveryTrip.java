package org.example.storemanager.entity.crm;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.sales.SaleOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DeliveryTrip extends BaseEntity {

    @Column(name = "trip_code", nullable = false, unique = true, length = 50)
    private String tripCode;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(precision = 18, scale = 2)
    private BigDecimal fee; // Phí giao hàng

    @Column(nullable = false, length = 30)
    private String status; // PENDING, DELIVERING, SUCCESS, FAILED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id", nullable = false)
    private Shipper shipper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private SaleOrder order;

    private Double distance; // Quãng đường

    @Column(name = "estimated_time")
    private Integer estimatedTime; // Thời gian dự kiến (phút)

    @Column(name = "actual_time")
    private Integer actualTime; // Thời gian thực tế (phút)

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress; // Địa chỉ giao

    @Column(name = "receiver_name", length = 150)
    private String receiverName; // Người nhận

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone; // SĐT

    @Column(name = "delivery_note", columnDefinition = "TEXT")
    private String deliveryNote; // Ghi chú giao hàng

    @Column(name = "proof_image", length = 500)
    private String proofImage; // Ảnh giao hàng

    @Column(length = 500)
    private String signature; // Chữ ký

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason; // Lý do hủy
}