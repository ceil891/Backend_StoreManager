package org.example.storemanager.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DatabaseMigrationRunner - Runs safe idempotent ALTER TABLE migrations on startup.
 * Uses IF NOT EXISTS so it's safe to run multiple times.
 * Order(1) ensures it runs first among ApplicationRunners.
 */
@Slf4j
@Component
@Lazy(false)
@Order(1)
@RequiredArgsConstructor
public class DatabaseMigrationConfig implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS customer_name VARCHAR(200)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(30)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS shipping_address TEXT"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS order_origin VARCHAR(50) DEFAULT 'ONLINE'"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) DEFAULT 'UNPAID'"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS carrier_id BIGINT"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS carrier VARCHAR(100)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS driver_id BIGINT"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS tracking_code VARCHAR(100)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS tracking_url VARCHAR(500)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS shipper_name VARCHAR(150)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS shipper_phone VARCHAR(30)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS delivery_status VARCHAR(50) DEFAULT 'UNASSIGNED'"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS assigned_by VARCHAR(100)"
            );

            // Audit Trail table for delivery assignments
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS delivery_assignment_histories (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "order_id BIGINT NOT NULL, " +
                "order_code VARCHAR(50), " +
                "carrier_id BIGINT, " +
                "carrier_name VARCHAR(150), " +
                "shipper_id BIGINT, " +
                "shipper_name VARCHAR(150), " +
                "shipper_phone VARCHAR(30), " +
                "tracking_code VARCHAR(100), " +
                "tracking_url VARCHAR(500), " +
                "delivery_status VARCHAR(50), " +
                "action_type VARCHAR(50), " +
                "assigned_at TIMESTAMP, " +
                "assigned_by VARCHAR(100), " +
                "note VARCHAR(500), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted BOOLEAN DEFAULT FALSE" +
                ")"
            );
            jdbcTemplate.execute(
                "UPDATE sale_orders SET order_origin = 'ONLINE' WHERE order_origin = 'ONLINE_STORE' OR order_origin IS NULL"
            );
            jdbcTemplate.execute(
                "UPDATE sale_orders SET payment_status = 'UNPAID' WHERE payment_status IS NULL"
            );
            jdbcTemplate.execute(
                "UPDATE sale_orders SET is_active = true WHERE is_active IS NULL"
            );
            try {
                jdbcTemplate.execute("ALTER TABLE sale_orders DROP CONSTRAINT IF EXISTS sale_orders_order_origin_check");
                jdbcTemplate.execute("ALTER TABLE sale_orders ADD CONSTRAINT sale_orders_order_origin_check CHECK (order_origin IN ('MANUAL', 'POS', 'ONLINE', 'ONLINE_STORE'))");
            } catch (Exception ex) {
                log.info("[Migration] sale_orders_order_origin_check constraint note: {}", ex.getMessage());
            }
            try {
                jdbcTemplate.execute("ALTER TABLE sale_orders DROP CONSTRAINT IF EXISTS sale_orders_status_check");
                jdbcTemplate.execute("ALTER TABLE sale_orders ADD CONSTRAINT sale_orders_status_check CHECK (status IN ('PENDING','CONFIRMED','PROCESSING','DELIVERING','SHIPPED','COMPLETED','DELIVERED','CANCELLED'))");
                log.info("[Migration] sale_orders_status_check constraint updated OK.");
            } catch (Exception ex) {
                log.info("[Migration] sale_orders_status_check constraint note: {}", ex.getMessage());
            }
            log.info("[Migration] sale_orders columns (customer_name, customer_phone, shipping_address, is_active, order_origin, payment_status) ensured OK.");

            // ---- sale_order_details migrations ----
            try {
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS unit_price NUMERIC(18, 2)");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(18, 2) DEFAULT 0");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(18, 2) DEFAULT 0");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS total_amount NUMERIC(18, 2)");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS order_id BIGINT");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS sale_order_id BIGINT");

                jdbcTemplate.execute("UPDATE sale_order_details SET unit_price = unit_price_snapshot WHERE unit_price IS NULL AND unit_price_snapshot IS NOT NULL");
                jdbcTemplate.execute("UPDATE sale_order_details SET total_amount = sub_total WHERE total_amount IS NULL AND sub_total IS NOT NULL");
                jdbcTemplate.execute("UPDATE sale_order_details SET sub_total = total_amount WHERE sub_total IS NULL AND total_amount IS NOT NULL");
                jdbcTemplate.execute("UPDATE sale_order_details SET sale_order_id = order_id WHERE sale_order_id IS NULL AND order_id IS NOT NULL");
                jdbcTemplate.execute("UPDATE sale_order_details SET order_id = sale_order_id WHERE order_id IS NULL AND sale_order_id IS NOT NULL");

                jdbcTemplate.execute("ALTER TABLE sale_order_details ALTER COLUMN unit_price DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ALTER COLUMN total_amount DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ALTER COLUMN sub_total DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ALTER COLUMN sale_order_id DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ALTER COLUMN order_id DROP NOT NULL");
                log.info("[Migration] sale_order_details columns (unit_price, discount_amount, tax_amount, total_amount, sub_total, sale_order_id, order_id) ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] sale_order_details migration warning: {}", ex.getMessage());
            }

            // ---- delivery_assignment_histories: add missing BaseEntity columns ----
            try {
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS created_by VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT false");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS trace_id VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS tenant_id BIGINT");
                jdbcTemplate.execute("UPDATE delivery_assignment_histories SET version = 0 WHERE version IS NULL");
                jdbcTemplate.execute("UPDATE delivery_assignment_histories SET is_deleted = false WHERE is_deleted IS NULL");
                jdbcTemplate.execute("UPDATE delivery_assignment_histories SET is_locked = false WHERE is_locked IS NULL");
                log.info("[Migration] delivery_assignment_histories BaseEntity columns ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] delivery_assignment_histories migration warning: {}", ex.getMessage());
            }
        } catch (Exception e) {
            log.warn("[Migration] sale_orders migration warning: {}", e.getMessage());
        }
    }
}
