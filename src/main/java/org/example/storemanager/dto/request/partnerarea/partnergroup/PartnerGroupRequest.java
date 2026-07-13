package org.example.storemanager.dto.request.partnerarea.partnergroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PartnerGroupRequest {
    @NotBlank(message = "Mã nhóm không được để trống")
    @Size(min = 2, max = 50, message = "Mã nhóm phải từ 2-50 ký tự")
    private String groupCode;

    @NotBlank(message = "Tên nhóm không được để trống")
    private String groupName;

    @NotBlank(message = "Loại đối tác không được để trống")
    private String type;

    private String description;

    private Boolean isActive;
}