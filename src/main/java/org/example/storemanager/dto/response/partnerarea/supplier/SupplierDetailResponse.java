package org.example.storemanager.dto.response.partnerarea.supplier;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class SupplierDetailResponse {
    private Long id;
    private String supplierCode;
    private String name;
    private String category;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String taxCode;

    // Các trường tài chính/công nợ
    private Integer paymentTerm;
    private BigDecimal creditLimit;
    private String bankName;
    private String bankAccount;
    private String accountHolder;
    private String description;

    private Boolean isActive;

    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String groupCode;
}