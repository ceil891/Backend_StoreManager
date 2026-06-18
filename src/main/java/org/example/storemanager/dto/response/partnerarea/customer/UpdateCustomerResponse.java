package org.example.storemanager.dto.response.partnerarea.customer;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UpdateCustomerResponse {
    private Long id;
    private String message;
    private LocalDateTime updatedAt;
    private String updatedBy;
}