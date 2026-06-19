package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String taxCode;
    private String note;
    private Double points;
    private Double totalSpend;
    private String membershipRank;
    private Boolean status;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}