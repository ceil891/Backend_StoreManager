package org.example.storemanager.modules.cart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_items_cart_id", columnList = "cart_id"),
    @Index(name = "idx_cart_items_cart_variant", columnList = "cart_id, product_variant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    /**
     * Luôn NOT NULL. Sản phẩm không có variant sẽ dùng Default Variant.
     * Đây là key để nhận dạng mặt hàng trong giỏ hàng.
     */
    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    // ── Snapshot data (bất biến sau khi thêm vào giỏ) ──────────────

    /** Tên sản phẩm tại thời điểm thêm vào giỏ. */
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    /** Tên variant (ví dụ: "38 Trắng", "128GB Titan Black"). Null nếu Default Variant. */
    @Column(name = "variant_name", length = 200)
    private String variantName;

    /** SKU tại thời điểm thêm vào giỏ. */
    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    /** URL ảnh thumbnail tại thời điểm thêm vào giỏ. */
    @Column(name = "thumbnail", length = 2000)
    private String thumbnail;

    /** Giá tại thời điểm thêm vào giỏ (snapshot – KHÔNG dùng cho checkout). */
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    // ──────────────────────────────────────────────────────────────────

    /** Số lượng, luôn >= 1. */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
