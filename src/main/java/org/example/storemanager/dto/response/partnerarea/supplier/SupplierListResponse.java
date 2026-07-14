package org.example.storemanager.dto.response.partnerarea.supplier;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SupplierListResponse {
    private Long id;
    private String supplierCode;
    private String name;
    private String category;
    private String contactPerson;

    private String phone;
    private BigDecimal creditLimit;

    private Boolean isActive;

    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String groupCode;
}