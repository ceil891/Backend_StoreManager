package org.example.storemanager.dto.request.partnerarea.customerdto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateCustomerRequest {
    @NotBlank(message = "Tên khách hàng không được để trống")
    private String name;

    private String phone;

    @Email(message = "Email không đúng định dạng")
    private String email;

    private String address;
    private String taxCode;
    private Long groupId; // ID của nhóm khách hàng
    private Long areaId;  // ID của khu vực
    private MultipartFile avatar;
}