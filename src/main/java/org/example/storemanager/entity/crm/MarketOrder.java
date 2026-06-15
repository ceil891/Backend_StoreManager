package org.example.storemanager.entity.crm;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.partnerarea.Customer;

import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}