package org.example.storemanager.modules.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Kết quả validate giỏ hàng trước khi checkout.
 * Nếu priceChanges không rỗng → frontend hỏi user xác nhận.
 * Nếu unavailableItems không rỗng → frontend thông báo và yêu cầu xóa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutValidationResult {

    private boolean valid;

    /** Danh sách sản phẩm có giá thay đổi so với giỏ hàng. */
    private List<PriceChangeAlert> priceChanges;

    /** Danh sách variantId ngừng bán hoặc không còn active. */
    private List<Long> unavailableVariantIds;

    /** Danh sách variantId không đủ tồn kho (reserved cho Phase 2). */
    private List<Long> outOfStockVariantIds;

    public static CheckoutValidationResult ok() {
        return CheckoutValidationResult.builder()
                .valid(true)
                .priceChanges(List.of())
                .unavailableVariantIds(List.of())
                .outOfStockVariantIds(List.of())
                .build();
    }
}
