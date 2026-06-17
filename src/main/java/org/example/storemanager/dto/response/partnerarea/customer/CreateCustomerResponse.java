package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCustomerResponse {
    private Long id;
    private String customerCode;
    private String message;
}