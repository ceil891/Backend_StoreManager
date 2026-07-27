package org.example.storemanager.modules.partnerarea.dto.request.customerdto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UpdateCustomerRequest {
    private String name;
    private String phone;
    private String email;
    private String address;
    private String taxCode;
    private Long groupId;
    private Long areaId;
    private Boolean isActive;
    private MultipartFile avatar;
    private String avatarUrl;// Cho phép cập nhật trạng thái hoạt động
}