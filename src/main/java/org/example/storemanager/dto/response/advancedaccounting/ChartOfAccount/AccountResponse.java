package org.example.storemanager.dto.response.advancedaccounting.ChartOfAccount;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.storemanager.enums.account.AccountType; // Import Enum của cậu

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Ẩn trường isDeleted khi nó là null
public class AccountResponse {
    private Long id;
    private String accountCode;
    private String accountName;
    private AccountType type; // Dùng Enum trực tiếp, không dùng String
    private Boolean isActive;
    private Boolean isDeleted;
    private Long parentId;
}