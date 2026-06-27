package org.example.storemanager.entity.partnerarea;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity {

    @Column(name = "customer_code", nullable = false, unique = true, length = 50)
    private String customerCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String address;

    private LocalDate dob; // Ngày sinh

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Column(name = "avatar_url")
    private String avatarUrl; // Lưu link ảnh (hoặc path file)

    @Column(name = "membership_rank", length = 20)
    private String membershipRank; // Đồng, Bạc, Vàng, Kim cương

    @Column(name = "points")
    private Double points = 0.0; // Điểm tích lũy

    @Column(name = "total_spend")
    private Double totalSpend = 0.0; // Tổng chi tiêu

    @Column(columnDefinition = "TEXT")
    private String note; // Ghi chú CSKH

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private PartnerGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;
}