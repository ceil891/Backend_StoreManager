package org.example.storemanager.modules.omnichannel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity
@Table(name = "shipping_carriers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ShippingCarrier extends BaseEntity {

    @Column(name = "carrier_code", nullable = false, unique = true, length = 50)
    private String carrierCode; // VD: GHN, GHTK, VTP, SPX

    @Column(name = "carrier_name", nullable = false, length = 150)
    private String carrierName; // VD: Giao Hàng Nhanh

    @Column(name = "tracking_url_format", length = 255)
    private String trackingUrlFormat; // Chuỗi URL định dạng để tra cứu (VD: https://donhang.ghn.vn/?order_code=%s)

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(length = 150)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String address;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(columnDefinition = "TEXT")
    private String notes;
}