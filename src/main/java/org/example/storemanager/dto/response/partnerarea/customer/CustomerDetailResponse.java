package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDetailResponse {
    private Long id;
    private String customerCode;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String taxCode;
    private String groupName;
    private String areaName;
}