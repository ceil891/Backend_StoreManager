package org.example.storemanager.modules.partnerarea.dto.response.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private Double points;
    private Double totalSpend;
    private String membershipRank;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private Boolean isActive;
    
    private LocalDate dob;
    private String taxCode;
    private String gender;
    private String note;
    private Long groupId;
    private Long areaId;
}