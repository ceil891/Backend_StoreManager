package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerListResponse {
    private Long id;
    private String customerCode;
    private String name;
    private String phone;
    private String email;
    private String membershipRank;
    private Boolean status;
    private String avatarUrl;
    private String address;
    private String taxCode;
}