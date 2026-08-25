package org.example.storemanager.modules.partnerarea.dto.response.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storemanager.modules.partnerarea.entity.CustomerAddress;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddressResponse {
    private Long id;
    private Long customerId;
    private String customerPhone;
    private String recipientName;
    private String phoneNumber;
    private String province;
    private String district;
    private String ward;
    private String street;
    private String fullAddress;
    private String addressType;
    private Boolean isDefault;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CustomerAddressResponse fromEntity(CustomerAddress entity) {
        if (entity == null) return null;
        return CustomerAddressResponse.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .customerPhone(entity.getCustomerPhone())
                .recipientName(entity.getRecipientName())
                .phoneNumber(entity.getPhoneNumber())
                .province(entity.getProvince())
                .district(entity.getDistrict())
                .ward(entity.getWard())
                .street(entity.getStreet())
                .fullAddress(entity.getFullAddress())
                .addressType(entity.getAddressType() != null ? entity.getAddressType() : "HOME")
                .isDefault(Boolean.TRUE.equals(entity.getIsDefault()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
