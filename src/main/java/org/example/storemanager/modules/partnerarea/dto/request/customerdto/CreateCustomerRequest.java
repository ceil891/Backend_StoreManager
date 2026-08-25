package org.example.storemanager.modules.partnerarea.dto.request.customerdto;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Data
@Builder
public class CreateCustomerRequest {
    @NotBlank(message = "Tên khách hàng không được để trống")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(09|\\+849)[0-9]{8}$", message = "Số điện thoại phải bắt đầu bằng đầu 09 và có đủ 10 chữ số")
    private String phone;

    @Email(message = "Email không đúng định dạng hợp lệ")
    private String email;

    private String address;
    private Long groupId;
    private Long areaId;
    private MultipartFile avatar;
    private Boolean isActive;
    private String avatarUrl;
    
    private LocalDate dob;
    private String gender;
    private String membershipRank;
    private String note;
    private String taxCode;
}