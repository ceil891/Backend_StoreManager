package org.example.storemanager.dto.response.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestDetailResponse {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal estimatedPrice;
}
