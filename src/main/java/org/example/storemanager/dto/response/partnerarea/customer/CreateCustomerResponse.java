package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CreateCustomerResponse {
    private Long id;
    private String avatarUrl;
    private String customerCode;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String taxCode;
    private String message;
    private LocalDateTime createdAt;
    private String createdBy;
}