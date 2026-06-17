package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCustomerResponse {
    private Long id;
    private String message;
    private String updatedAt;
}