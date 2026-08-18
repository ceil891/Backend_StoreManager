package org.example.storemanager.modules.sales.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerReturnDetailResponse {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal refundPrice;
    private BigDecimal subTotal;

    public static CustomerReturnDetailResponseBuilder builder() {
        return new CustomerReturnDetailResponseBuilder();
    }

    public static class CustomerReturnDetailResponseBuilder {
        private Long id;
        private Long productId;
        private String productCode;
        private String productName;
        private BigDecimal quantity;
        private BigDecimal refundPrice;
        private BigDecimal subTotal;

        public CustomerReturnDetailResponseBuilder id(Long id) { this.id = id; return this; }
        public CustomerReturnDetailResponseBuilder productId(Long productId) { this.productId = productId; return this; }
        public CustomerReturnDetailResponseBuilder productCode(String productCode) { this.productCode = productCode; return this; }
        public CustomerReturnDetailResponseBuilder productName(String productName) { this.productName = productName; return this; }
        public CustomerReturnDetailResponseBuilder quantity(BigDecimal quantity) { this.quantity = quantity; return this; }
        public CustomerReturnDetailResponseBuilder refundPrice(BigDecimal refundPrice) { this.refundPrice = refundPrice; return this; }
        public CustomerReturnDetailResponseBuilder subTotal(BigDecimal subTotal) { this.subTotal = subTotal; return this; }

        public CustomerReturnDetailResponse build() {
            return new CustomerReturnDetailResponse(id, productId, productCode, productName, quantity, refundPrice, subTotal);
        }
    }
}
