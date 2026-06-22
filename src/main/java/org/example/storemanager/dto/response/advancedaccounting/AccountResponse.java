package org.example.storemanager.dto.response.advancedaccounting;
import lombok.*;

@Data @Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {
    private Long id;
    private String accountCode;
    private String accountName;
    private String type;
    private Boolean isActive;
    private Long parentId;
}