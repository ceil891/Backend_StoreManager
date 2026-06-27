package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DeleteCustomerResponse {
    private Long id;
    private String message;
    private LocalDateTime deletedAt;
    private String deletedBy;
}