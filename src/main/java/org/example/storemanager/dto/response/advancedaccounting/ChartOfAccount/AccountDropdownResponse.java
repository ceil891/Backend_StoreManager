package org.example.storemanager.dto.response.advancedaccounting.ChartOfAccount;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDropdownResponse {
    private Long id;
    private String label; // Kết hợp Code + Name cho dễ chọn
    private String value;
}