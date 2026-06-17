package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerListResponse {
    private Long id;
    private String name;
    private String phone;
    private Boolean isActive;
}