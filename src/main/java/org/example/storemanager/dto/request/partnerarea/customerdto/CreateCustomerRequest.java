package org.example.storemanager.dto.request.partnerarea.customerdto;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class CreateCustomerRequest {
    @NotBlank(message = "Tên khách hàng không được để trống")
    private String name;

    // Thêm validate cho số điện thoại để tránh dữ liệu rác
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[35789])[0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Email(message = "Email không đúng định dạng")
    private String email;

    private String address;
    private Long groupId;
    private Long areaId;
    private MultipartFile avatar;
    private Boolean isActive;
    private String avatarUrl;
}