package org.example.storemanager.modules.partnerarea.dto.request.supplier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import lombok.Data;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CreateSupplierRequest {
    private String supplierCode;

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String name;

    private String category;

    private String contactPerson;

    // Validate phone
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải từ 10-11 chữ số")
    private String phone;

    // Validate email
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String address;
    private String taxCode;

    // Validate tài chính
    @PositiveOrZero(message = "Hạn mức công nợ không được âm")
    private BigDecimal creditLimit;
    private Integer paymentTerm;
    private String bankName;
    private String bankAccount;
    private String accountHolder;
    private String description;

    private Long groupId;
    private Long areaId;
    private Boolean isActive;



}