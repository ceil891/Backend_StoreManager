package org.example.storemanager.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Các database hiện hữu đang dùng ddl-auto=none và không chạy schema.sql.
 * Migration nhỏ này đảm bảo các cột mới được tạo khi nâng cấp phiên bản.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaCompatibilityMigration {
    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void apply() {
        addColumnIfMissing("users", "face_descriptor", "TEXT");
        addColumnIfMissing("attendances", "branch_id", "BIGINT");
        // Online/POS returns may reference sale_orders directly, without an invoice.
        // Changing @JoinColumn(nullable = true) does not migrate existing databases.
        jdbcTemplate.execute("ALTER TABLE IF EXISTS customer_returns ALTER COLUMN invoice_id DROP NOT NULL");
        log.info("Schema customer_returns.invoice_id now allows returns linked directly to an order");
    }

    private void addColumnIfMissing(String table, String column, String type) {
        try {
            jdbcTemplate.execute("ALTER TABLE IF EXISTS " + table
                    + " ADD COLUMN IF NOT EXISTS " + column + " " + type);
        } catch (Exception ex) {
            // Không chặn khởi động nếu một service không sở hữu bảng này.
            log.warn("Không thể cập nhật schema {}.{}: {}", table, column, ex.getMessage());
        }
    }
}
