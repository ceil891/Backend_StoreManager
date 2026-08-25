package org.example.storemanager.modules.partnerarea.dto.request.customerdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddressRequest {
    private Long customerId;
    private String customerPhone;
    private String recipientName;
    private String phoneNumber;
    private String province;
    private String district;
    private String ward;
    private String street;
    private String fullAddress;
    private String addressType; // HOME, OFFICE, OTHER
    private Boolean isDefault;
    private String notes;
}
