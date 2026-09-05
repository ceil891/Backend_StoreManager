package org.example.storemanager.modules.partnerarea.dto.response.customer;

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
    private String avatarUrl;
    private String address;
    private Double points;
    private Double totalSpend;
    private Boolean isActive;
    private String taxCode;
    private String gender;
    private java.time.LocalDate dob;
    private Double debtLimit;
    private Long groupId;
    private String groupName;
    private Long areaId;
    private String areaName;
    private String note;
    private Boolean isCreditBlocked;
}