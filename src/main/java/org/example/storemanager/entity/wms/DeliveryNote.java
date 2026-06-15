package org.example.storemanager.entity.wms;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DeliveryNote extends BaseEntity {

    @Column(name = "note_code", nullable = false, unique = true, length = 50)
    private String noteCode;

    @Column(name = "delivery_date", nullable = false)
    private LocalDateTime deliveryDate;

    @Column(name = "recipient_name", length = 150)
    private String recipientName; // Tên người ký nhận hàng thực tế

    @Column(nullable = false, length = 30)
    private String status; // DELIVERING, SUCCESS, FAILED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packing_list_id", nullable = false)
    private PackingList packingList; // Dựa trên phiếu đóng gói nào
}