package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerListResponse {
    private Long id;
    private String name;
    private String phone;
    private Boolean isActive;
}