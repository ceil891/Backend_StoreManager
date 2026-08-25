package org.example.storemanager.modules.system.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotNull(message = "Role ID không được để trống")
    private Long roleId;

    private Long branchId;
    private String status;

    private String taxId;
    private String identityId;
    private String dateOfBirth;
    private String departmentId;
    private String positionId;
    private String avatar;
}