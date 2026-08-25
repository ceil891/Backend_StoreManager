package org.example.storemanager.modules.partnerarea.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity
@Table(name = "customer_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CustomerAddress extends BaseEntity {

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_phone", length = 25)
    private String customerPhone;

    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Column(name = "phone_number", nullable = false, length = 25)
    private String phoneNumber;

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "ward", length = 100)
    private String ward;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "full_address", columnDefinition = "TEXT")
    private String fullAddress;

    @Column(name = "address_type", length = 30)
    @Builder.Default
    private String addressType = "HOME"; // HOME (Nhà riêng), OFFICE (Văn phòng), OTHER (Khác)

    @Column(name = "is_default", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "notes", length = 255)
    private String notes;

    @PrePersist
    @PreUpdate
    public void generateFullAddress() {
        if (this.fullAddress == null || this.fullAddress.isBlank()) {
            StringBuilder sb = new StringBuilder();
            if (this.street != null && !this.street.isBlank()) sb.append(this.street.trim());
            if (this.ward != null && !this.ward.isBlank()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(this.ward.trim());
            }
            if (this.district != null && !this.district.isBlank()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(this.district.trim());
            }
            if (this.province != null && !this.province.isBlank()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(this.province.trim());
            }
            this.fullAddress = sb.toString();
        }
        if (this.addressType == null) {
            this.addressType = "HOME";
        }
        if (this.isDefault == null) {
            this.isDefault = false;
        }
    }
}
