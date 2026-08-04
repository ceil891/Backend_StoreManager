package org.example.storemanager.modules.cart.repository;

import org.example.storemanager.modules.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    /**
     * Tìm item theo cart + variantId.
     * Dùng để kiểm tra trùng khi thêm vào giỏ (không cần productId nữa).
     */
    Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long productVariantId);

    void deleteByCartId(Long cartId);
}
