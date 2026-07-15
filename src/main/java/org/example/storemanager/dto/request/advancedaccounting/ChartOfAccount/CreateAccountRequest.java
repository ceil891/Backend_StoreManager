package org.example.storemanager.dto.request.advancedaccounting.ChartOfAccount;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import org.example.storemanager.enums.account.AccountType;

@Data
public class CreateAccountRequest {
    @NotBlank(message = "Mã tài khoản không được trống")
    private String accountCode;
    @NotBlank(message = "Tên tài khoản không được trống")
    private String accountName;
    private AccountType type;
    private Long parentId;
    private Boolean isActive = true;
}