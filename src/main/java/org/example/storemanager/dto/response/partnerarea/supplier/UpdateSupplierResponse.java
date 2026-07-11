package org.example.storemanager.dto.response.partnerarea.supplier;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UpdateSupplierResponse {
    private Long id;
    private String supplierCode;
    private String name;
    private String category;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String taxCode;
    private Integer paymentTerm;
    private BigDecimal creditLimit;
    private Boolean isActive;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String bankName;
    private String bankAccount;
    private String accountHolder;
    private String description;
    private String message;
}