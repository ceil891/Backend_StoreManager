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
public class UpdateCustomerResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String avatarUrl;
    private String membershipRank;
    private Double points;
    private Double totalSpend;
    private String message;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Boolean isActive;

    private LocalDate dob;
    private String taxCode;
    private String gender;
    private String note;
    private Long groupId;
    private Long areaId;
}