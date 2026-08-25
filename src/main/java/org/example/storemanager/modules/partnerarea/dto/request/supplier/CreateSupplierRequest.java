package org.example.storemanager.modules.partnerarea.dto.request.supplier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplierRequest {
    @NotBlank(message = "Mã nhà cung cấp không được để trống")
    private String supplierCode;

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Size(min = 3, message = "Tên nhà cung cấp phải từ 3 ký tự trở lên")
    @Pattern(regexp = "^(?!\\d+$)[a-zA-Z0-9À-ỹ\\s\\-\\(\\)\\.,&]+$", message = "Tên nhà cung cấp không hợp lệ và không được chỉ chứa chữ số")
    private String name;

    private String category;

    private String contactPerson;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[3|5|7|8|9]\\d{8}$", message = "Số điện thoại không hợp lệ (phải gồm 10 số bắt đầu bằng 03, 05, 07, 08, 09)")
    private String phone;

    // Validate email
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Địa chỉ nhà cung cấp không được để trống")
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