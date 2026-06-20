package org.example.storemanager.dto.request.partnerarea.customerdto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateCustomerRequest {
    private String name;
    private String phone;
    private String email;
    private String address;
    private String taxCode;
    private Long groupId;
    private Long areaId;
    private String isActive;
    private MultipartFile avatar;// Cho phép cập nhật trạng thái hoạt động
}