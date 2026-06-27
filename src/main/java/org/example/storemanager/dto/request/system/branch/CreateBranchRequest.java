package org.example.storemanager.dto.request.system.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBranchRequest {

    @NotBlank(message = "Mã chi nhánh không được để trống")
    @Size(max = 50, message = "Mã chi nhánh không được quá 50 ký tự")
    private String branchCode;

    @NotBlank(message = "Tên chi nhánh không được để trống")
    @Size(max = 150, message = "Tên chi nhánh không được quá 150 ký tự")
    private String branchName;

    @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự")
    private String address;

    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String phone;

    private Boolean isActive = true;

    private Long managerId;
}
