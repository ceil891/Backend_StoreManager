package org.example.storemanager.dto.response.purchase;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PurchaseDashboardSummaryResponse {
    private BigDecimal totalSpending;
    private Long totalOrdersCount;
    private Long pendingRequestsCount;
    private Long activeContractsCount;
    private List<SupplierSpending> topSuppliers;
    private List<ProductPurchased> topProducts;

    @Data
    @Builder
    public static class SupplierSpending {
        private Long supplierId;
        private String supplierName;
        private BigDecimal amount;
    }

    @Data
    @Builder
    public static class ProductPurchased {
        private Long productId;
        private String productName;
        private BigDecimal quantity;
    }
}
