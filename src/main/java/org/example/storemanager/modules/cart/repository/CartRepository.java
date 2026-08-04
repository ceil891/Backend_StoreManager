package org.example.storemanager.modules.cart.repository;

import org.example.storemanager.modules.cart.entity.Cart;
import org.example.storemanager.shared.enums.cart.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /** Tìm ACTIVE cart của user đã đăng nhập. */
    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

    /** Tìm ACTIVE cart theo guestToken. */
    Optional<Cart> findByGuestTokenAndStatus(String guestToken, CartStatus status);

    /** Lấy các cart ACTIVE đã hết TTL (dùng trong scheduler). */
    List<Cart> findByStatusAndExpiresAtBefore(CartStatus status, LocalDateTime threshold);

    /** Xóa hẳn các cart EXPIRED đã quá 30 ngày (audit: mark EXPIRED trước, xóa sau). */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.status = :status AND c.expiresAt < :threshold")
    void deleteByStatusAndExpiresAtBefore(@Param("status") CartStatus status,
                                          @Param("threshold") LocalDateTime threshold);
}
