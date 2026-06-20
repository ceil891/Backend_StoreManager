package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UpdateCustomerResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String avatarUrl;
    private String membershipRank;
    private String message;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private String status;
}