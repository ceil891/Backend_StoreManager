package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
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
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}