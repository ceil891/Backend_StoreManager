package org.example.storemanager.dto.response.advancedaccounting;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storemanager.enums.account.AccountType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailResponse {
    private Long id;
    private String accountCode;
    private String accountName;
    private AccountType type;
    @JsonProperty("isActive")
    private Boolean isActive;
    @JsonProperty("isDeleted")
    private Boolean isDeleted;
    private Long parentId;
}
