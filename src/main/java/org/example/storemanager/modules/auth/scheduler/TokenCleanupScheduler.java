package org.example.storemanager.modules.auth.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.system.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Chạy định kỳ lúc 03:00 sáng mỗi ngày để dọn dẹp các Refresh Token đã hết hạn hoặc đã bị thu hồi
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("[TokenCleanupScheduler] Bắt đầu dọn dẹp Refresh Token hết hạn hoặc đã bị thu hồi...");
        try {
            LocalDateTime now = LocalDateTime.now();
            refreshTokenRepository.deleteExpiredAndRevoked(now);
            log.info("[TokenCleanupScheduler] Dọn dẹp Refresh Token hoàn tất thành công.");
        } catch (Exception e) {
            log.error("[TokenCleanupScheduler] Lỗi khi dọn dẹp Refresh Token: {}", e.getMessage(), e);
        }
    }
}