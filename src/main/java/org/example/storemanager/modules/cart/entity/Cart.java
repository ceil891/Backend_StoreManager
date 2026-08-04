package org.example.storemanager.modules.cart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.shared.enums.cart.CartStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_carts_user_id_status", columnList = "user_id, status"),
    @Index(name = "idx_carts_guest_token_status", columnList = "guest_token, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = "items")
@ToString(exclude = "items")
public class Cart extends BaseEntity {

    /**
     * User owner – null nếu là guest cart.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    /**
     * SecureRandom 128-bit Base64URL token cho guest cart.
     * Null nếu là user cart.
     */
    @Column(name = "guest_token", length = 64, nullable = true, unique = true)
    private String guestToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CartStatus status = CartStatus.ACTIVE;

    /**
     * Guest cart hết hạn sau 30 ngày.
     * User cart: null (không expire).
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
}
