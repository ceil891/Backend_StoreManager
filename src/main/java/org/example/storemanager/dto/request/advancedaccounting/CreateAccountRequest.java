package org.example.storemanager.dto.request.advancedaccounting;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateAccountRequest {
    @NotBlank(message = "Mã tài khoản không được trống")
    private String accountCode;
    @NotBlank(message = "Tên tài khoản không được trống")
    private String accountName;
    private String type;
    private Long parentId;
    private Boolean isActive = true;
}