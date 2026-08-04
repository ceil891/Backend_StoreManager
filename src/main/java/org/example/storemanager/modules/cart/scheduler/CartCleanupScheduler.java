package org.example.storemanager.modules.cart.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.cart.entity.Cart;
import org.example.storemanager.modules.cart.repository.CartRepository;
import org.example.storemanager.shared.enums.cart.CartStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job dọn dẹp guest cart hết hạn.
 *
 * Luồng audit-friendly:
 *   ACTIVE → EXPIRED  (khi quá expiresAt)
 *   EXPIRED → DELETE  (sau thêm 30 ngày nữa)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartCleanupScheduler {

    private final CartRepository cartRepository;

    /**
     * Chạy lúc 2:00 AM hàng ngày.
     * Bước 1: Mark ACTIVE cart đã quá TTL → EXPIRED.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expireOverdueCarts() {
        LocalDateTime now = LocalDateTime.now();
        List<Cart> expiredCarts = cartRepository.findByStatusAndExpiresAtBefore(CartStatus.ACTIVE, now);

        if (expiredCarts.isEmpty()) {
            log.debug("CartCleanupScheduler: không có cart nào hết hạn.");
            return;
        }

        expiredCarts.forEach(cart -> cart.setStatus(CartStatus.EXPIRED));
        cartRepository.saveAll(expiredCarts);
        log.info("CartCleanupScheduler: đã mark {} cart sang EXPIRED.", expiredCarts.size());
    }

    /**
     * Chạy lúc 3:00 AM hàng ngày.
     * Bước 2: Xóa hẳn các cart EXPIRED đã quá 30 ngày thêm.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteAgedExpiredCarts() {
        // Cart đã EXPIRED và expiresAt đã quá 30 ngày trước
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        cartRepository.deleteByStatusAndExpiresAtBefore(CartStatus.EXPIRED, threshold);
        log.info("CartCleanupScheduler: đã xóa các cart EXPIRED cũ hơn 30 ngày.");
    }
}
