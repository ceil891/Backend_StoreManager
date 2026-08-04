package org.example.storemanager.modules.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Thông tin cảnh báo khi giá sản phẩm thay đổi tại thời điểm checkout.
 * Frontend sẽ hiển thị dialog "Giá đã thay đổi, bạn có muốn tiếp tục?"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceChangeAlert {

    private Long variantId;
    private String productName;
    private String variantName;
    private String sku;

    /** Giá trong giỏ hàng (snapshot). */
    private BigDecimal cartPrice;

    /** Giá thực tế hiện tại của variant. */
    private BigDecimal currentPrice;

    private BigDecimal priceDiff;
}
